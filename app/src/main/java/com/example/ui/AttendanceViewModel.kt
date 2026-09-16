package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AttendanceRepository
import com.example.location.AttendanceLocationManager
import com.example.location.Coordinates
import com.example.model.AttendanceRecord
import com.example.model.AttendanceType
import com.example.model.AuthState
import com.example.model.EmployeeProfile
import com.example.model.LogDirection
import com.example.model.SocketConfig
import com.example.model.SocketLogEntry
import com.example.network.ConnectionStatus
import com.example.network.Gt06SocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)
    private val locationManager = AttendanceLocationManager(application)
    private val socketClient = Gt06SocketClient()

    val employeeProfile: StateFlow<EmployeeProfile> = repository.employeeProfile
    val socketConfig: StateFlow<SocketConfig> = repository.socketConfig
    val lastTimeIn: StateFlow<AttendanceRecord?> = repository.lastTimeIn
    val lastTimeOut: StateFlow<AttendanceRecord?> = repository.lastTimeOut

    val unsyncedCount: StateFlow<Int> = repository.unsyncedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val connectionStatus: StateFlow<ConnectionStatus> = socketClient.connectionStatus
    val logs: StateFlow<List<SocketLogEntry>> = socketClient.logs

    private val _currentCoordinates = MutableStateFlow(locationManager.defaultCoordinates)
    val currentCoordinates: StateFlow<Coordinates> = _currentCoordinates.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()

    private val _isProcessingTimeIn = MutableStateFlow(false)
    val isProcessingTimeIn: StateFlow<Boolean> = _isProcessingTimeIn.asStateFlow()

    private val _isProcessingTimeOut = MutableStateFlow(false)
    val isProcessingTimeOut: StateFlow<Boolean> = _isProcessingTimeOut.asStateFlow()

    private val _serverErrorMessage = MutableStateFlow<String?>(null)
    val serverErrorMessage: StateFlow<String?> = _serverErrorMessage.asStateFlow()

    private val _activeDialogRecord = MutableStateFlow<AttendanceRecord?>(null)
    val activeDialogRecord: StateFlow<AttendanceRecord?> = _activeDialogRecord.asStateFlow()

    private val _isDialogAlreadyMarked = MutableStateFlow(false)
    val isDialogAlreadyMarked: StateFlow<Boolean> = _isDialogAlreadyMarked.asStateFlow()

    private val _authState = MutableStateFlow(
        AuthState(
            isAuthenticated = repository.employeeProfile.value.companyCode.isNotBlank() &&
                    repository.employeeProfile.value.employeeCode.isNotBlank(),
            employeeId = repository.employeeProfile.value.employeeCode
        )
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            syncOfflineRecords()
        }
    }

    init {
        // Initial log and location fetch
        socketClient.addLog(
            LogDirection.INFO,
            "Presence VTP Attendance Client initialized (Powered by VTP)"
        )
        refreshLocation()

        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
    }

    fun syncOfflineRecords() {
        if (_isSyncing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                val pending = repository.getPendingOfflineRecords()
                if (pending.isEmpty()) return@launch

                socketClient.addLog(LogDirection.INFO, "Auto-syncing ${pending.size} offline record(s) to GT06 server...")
                var syncedCount = 0
                val config = socketConfig.value
                for (item in pending) {
                    val isTimeIn = item.type == AttendanceType.TIME_IN.name
                    val (ok, err) = socketClient.sendAttendance(
                        config = config,
                        lat = item.latitude,
                        lon = item.longitude,
                        isTimeIn = isTimeIn,
                        speedKmh = item.speedKmh,
                        courseAngle = item.courseAngle,
                        satellitesCount = item.satellitesCount,
                        altitudeMeters = item.altitudeMeters,
                        timestampMs = item.timestamp
                    )
                    if (ok) {
                        repository.markRecordSynced(item.id)
                        syncedCount++
                        socketClient.addLog(LogDirection.INFO, "Successfully synced offline ${item.type} from ${item.formattedDateTime}")
                    } else {
                        socketClient.addLog(LogDirection.WARN, "Sync postponed: Server returned: $err. Will retry when connection stabilizes.")
                        break
                    }
                }
                if (syncedCount > 0) {
                    socketClient.addLog(LogDirection.INFO, "Offline sync complete: $syncedCount record(s) transmitted.")
                }
            } catch (e: Exception) {
                socketClient.addLog(LogDirection.ERROR, "Sync error: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    val pakistanPresets = locationManager.pakistanPresets

    fun hasLocationPermission(): Boolean = locationManager.hasLocationPermission()
    fun isLocationServiceEnabled(): Boolean = locationManager.isLocationServiceEnabled()

    fun selectLocation(coordinates: Coordinates) {
        locationManager.setManualOverride(coordinates)
        _currentCoordinates.value = coordinates
        val locName = coordinates.addressName ?: coordinates.formatCoordinates()
        repository.updateProfileLocation(locName)
        socketClient.addLog(
            LogDirection.INFO,
            "Location set to: $locName (${coordinates.formatCoordinates()})"
        )
    }

    fun useHardwareGps() {
        locationManager.setManualOverride(null)
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _isLoadingLocation.value = true
            try {
                val coords = locationManager.getCurrentLocation()
                _currentCoordinates.value = coords
                if (coords.isRealGps && !coords.addressName.isNullOrBlank()) {
                    repository.updateProfileLocation(coords.addressName)
                }
                val providerStr = coords.provider ?: if (coords.isRealGps) "Real Device GPS" else "Default"
                val addrStr = coords.addressName?.let { " • $it" } ?: ""
                socketClient.addLog(
                    LogDirection.INFO,
                    "Acquired coordinates: ${coords.formatCoordinates()} (Accuracy: ±${coords.accuracyMeters.toInt()}m, Source: $providerStr$addrStr)"
                )
            } finally {
                _isLoadingLocation.value = false
            }
        }
    }

    fun onTimeInClicked() {
        if (_isProcessingTimeIn.value) return

        val existing = lastTimeIn.value
        if (existing != null) {
            _activeDialogRecord.value = existing
            socketClient.addLog(LogDirection.INFO, "Time In was already marked at ${existing.formattedDateTime}")
            return
        }

        viewModelScope.launch {
            _isProcessingTimeIn.value = true
            _serverErrorMessage.value = null
            try {
                // Ensure fresh location
                try {
                    withTimeoutOrNull(3000) {
                        val freshCoords = locationManager.getCurrentLocation()
                        _currentCoordinates.value = freshCoords
                        if (freshCoords.isRealGps && !freshCoords.addressName.isNullOrBlank()) {
                            repository.updateProfileLocation(freshCoords.addressName)
                        }
                    }
                } catch (_: Exception) {}

                val coords = _currentCoordinates.value
                val profile = employeeProfile.value
                val resolvedLocName = coords.addressName?.takeIf { it.isNotBlank() } ?: profile.location
                val config = socketConfig.value

                // Transmit to server and require successful handshake
                val (success, message) = socketClient.sendAttendance(
                    config = config,
                    lat = coords.latitude,
                    lon = coords.longitude,
                    isTimeIn = true,
                    speedKmh = coords.speedKmh,
                    courseAngle = coords.bearing,
                    satellitesCount = coords.satellitesCount,
                    altitudeMeters = coords.altitudeMeters
                )

                if (success) {
                    val record = repository.saveAttendanceRecord(
                        type = AttendanceType.TIME_IN,
                        lat = coords.latitude,
                        lon = coords.longitude,
                        txHex = "0x12 GPS Location (Speed: ${coords.formatSpeed()}, Angle: ${coords.formatBearing()})",
                        rxHex = "Transmitted OK",
                        locationNameOverride = resolvedLocName,
                        speedKmh = coords.speedKmh,
                        courseAngle = coords.bearing,
                        satellitesCount = coords.satellitesCount,
                        altitudeMeters = coords.altitudeMeters,
                        isSynced = true
                    )
                    _activeDialogRecord.value = record
                    socketClient.addLog(
                        LogDirection.INFO,
                        "Time In marked successfully for ${profile.name.ifBlank { profile.employeeCode }} at ${record.formattedDateTime}"
                    )
                } else {
                    // Offline Fallback: Save in Room database so attendance is never lost
                    val record = repository.saveAttendanceRecord(
                        type = AttendanceType.TIME_IN,
                        lat = coords.latitude,
                        lon = coords.longitude,
                        txHex = "0x12 GPS Location (Speed: ${coords.formatSpeed()}, Angle: ${coords.formatBearing()})",
                        rxHex = null,
                        locationNameOverride = resolvedLocName,
                        speedKmh = coords.speedKmh,
                        courseAngle = coords.bearing,
                        satellitesCount = coords.satellitesCount,
                        altitudeMeters = coords.altitudeMeters,
                        isSynced = false
                    )
                    _activeDialogRecord.value = record
                    val errorDetail = if (message.isNotBlank()) message else "No internet/server unreachable"
                    socketClient.addLog(LogDirection.WARN, "Time In saved locally in offline mode ($errorDetail). Will auto-sync when online.")
                }
            } catch (e: Exception) {
                val coords = _currentCoordinates.value
                val profile = employeeProfile.value
                val resolvedLocName = coords.addressName?.takeIf { it.isNotBlank() } ?: profile.location
                val record = repository.saveAttendanceRecord(
                    type = AttendanceType.TIME_IN,
                    lat = coords.latitude,
                    lon = coords.longitude,
                    txHex = null,
                    rxHex = null,
                    locationNameOverride = resolvedLocName,
                    speedKmh = coords.speedKmh,
                    courseAngle = coords.bearing,
                    satellitesCount = coords.satellitesCount,
                    altitudeMeters = coords.altitudeMeters,
                    isSynced = false
                )
                _activeDialogRecord.value = record
                val err = e.localizedMessage ?: "Network error"
                socketClient.addLog(LogDirection.WARN, "Time In stored in offline database ($err). Will auto-sync when online.")
            } finally {
                _isProcessingTimeIn.value = false
            }
        }
    }

    fun onTimeOutClicked() {
        if (_isProcessingTimeOut.value) return

        val existing = lastTimeOut.value
        if (existing != null) {
            _activeDialogRecord.value = existing
            socketClient.addLog(LogDirection.INFO, "Time Out was already marked at ${existing.formattedDateTime}")
            return
        }

        viewModelScope.launch {
            _isProcessingTimeOut.value = true
            _serverErrorMessage.value = null
            try {
                try {
                    withTimeoutOrNull(3000) {
                        val freshCoords = locationManager.getCurrentLocation()
                        _currentCoordinates.value = freshCoords
                        if (freshCoords.isRealGps && !freshCoords.addressName.isNullOrBlank()) {
                            repository.updateProfileLocation(freshCoords.addressName)
                        }
                    }
                } catch (_: Exception) {}

                val coords = _currentCoordinates.value
                val profile = employeeProfile.value
                val resolvedLocName = coords.addressName?.takeIf { it.isNotBlank() } ?: profile.location
                val config = socketConfig.value

                val (success, message) = socketClient.sendAttendance(
                    config = config,
                    lat = coords.latitude,
                    lon = coords.longitude,
                    isTimeIn = false,
                    speedKmh = coords.speedKmh,
                    courseAngle = coords.bearing,
                    satellitesCount = coords.satellitesCount,
                    altitudeMeters = coords.altitudeMeters
                )

                if (success) {
                    val record = repository.saveAttendanceRecord(
                        type = AttendanceType.TIME_OUT,
                        lat = coords.latitude,
                        lon = coords.longitude,
                        txHex = "0x12 GPS Location (Speed: ${coords.formatSpeed()}, Angle: ${coords.formatBearing()})",
                        rxHex = "Transmitted OK",
                        locationNameOverride = resolvedLocName,
                        speedKmh = coords.speedKmh,
                        courseAngle = coords.bearing,
                        satellitesCount = coords.satellitesCount,
                        altitudeMeters = coords.altitudeMeters,
                        isSynced = true
                    )
                    _activeDialogRecord.value = record
                    socketClient.addLog(
                        LogDirection.INFO,
                        "Time Out marked successfully for ${profile.name.ifBlank { profile.employeeCode }} at ${record.formattedDateTime}"
                    )
                } else {
                    // Offline Fallback: Save in Room database so attendance is never lost
                    val record = repository.saveAttendanceRecord(
                        type = AttendanceType.TIME_OUT,
                        lat = coords.latitude,
                        lon = coords.longitude,
                        txHex = "0x12 GPS Location (Speed: ${coords.formatSpeed()}, Angle: ${coords.formatBearing()})",
                        rxHex = null,
                        locationNameOverride = resolvedLocName,
                        speedKmh = coords.speedKmh,
                        courseAngle = coords.bearing,
                        satellitesCount = coords.satellitesCount,
                        altitudeMeters = coords.altitudeMeters,
                        isSynced = false
                    )
                    _activeDialogRecord.value = record
                    val errorDetail = if (message.isNotBlank()) message else "No internet/server unreachable"
                    socketClient.addLog(LogDirection.WARN, "Time Out saved locally in offline mode ($errorDetail). Will auto-sync when online.")
                }
            } catch (e: Exception) {
                val coords = _currentCoordinates.value
                val profile = employeeProfile.value
                val resolvedLocName = coords.addressName?.takeIf { it.isNotBlank() } ?: profile.location
                val record = repository.saveAttendanceRecord(
                    type = AttendanceType.TIME_OUT,
                    lat = coords.latitude,
                    lon = coords.longitude,
                    txHex = null,
                    rxHex = null,
                    locationNameOverride = resolvedLocName,
                    speedKmh = coords.speedKmh,
                    courseAngle = coords.bearing,
                    satellitesCount = coords.satellitesCount,
                    altitudeMeters = coords.altitudeMeters,
                    isSynced = false
                )
                _activeDialogRecord.value = record
                val err = e.localizedMessage ?: "Network error"
                socketClient.addLog(LogDirection.WARN, "Time Out stored in offline database ($err). Will auto-sync when online.")
            } finally {
                _isProcessingTimeOut.value = false
            }
        }
    }

    fun dismissServerError() {
        _serverErrorMessage.value = null
    }

    fun resetTodayAttendance() {
        repository.resetBiometric()
        _activeDialogRecord.value = null
        socketClient.addLog(LogDirection.INFO, "Today's attendance records reset.")
    }

    fun dismissDialog() {
        _activeDialogRecord.value = null
        _isDialogAlreadyMarked.value = false
    }

    fun resetBiometric() {
        repository.resetBiometric()
        socketClient.addLog(LogDirection.INFO, "Biometric cache reset. Time In and Time Out cleared.")
    }

    fun sendTestLoginPacket() {
        viewModelScope.launch {
            val config = socketConfig.value
            val coords = _currentCoordinates.value
            socketClient.sendAttendance(
                config = config,
                lat = coords.latitude,
                lon = coords.longitude,
                isTimeIn = true
            )
        }
    }

    fun clearLogs() {
        socketClient.clearLogs()
    }

    fun loginWithDetails(
        companyCode: String,
        employeeCode: String,
        name: String = "",
        designation: String = "",
        location: String = "",
        serverDigits: String = "01"
    ) {
        _activeDialogRecord.value = null
        _serverErrorMessage.value = null
        _isDialogAlreadyMarked.value = false
        repository.loginWithDetails(companyCode, employeeCode, name, designation, location, serverDigits)
        _authState.value = AuthState(
            isAuthenticated = true,
            employeeId = employeeCode,
            loginTime = System.currentTimeMillis()
        )
        val config = repository.socketConfig.value
        val displayName = name.ifBlank { "Employee #$employeeCode" }
        socketClient.addLog(
            LogDirection.INFO,
            "Logged in & Session Refreshed | $displayName ($designation) | Company: $companyCode, Emp: $employeeCode, Srv: $serverDigits | Location: $location | IMEI: ${config.imei}"
        )
    }

    fun loginWithCodes(companyCode: String, employeeCode: String, serverDigits: String = "01") {
        loginWithDetails(companyCode, employeeCode, serverDigits = serverDigits)
    }

    fun loginSuccess(employeeId: String? = null) {
        val empId = employeeId ?: employeeProfile.value.employeeId
        _activeDialogRecord.value = null
        _serverErrorMessage.value = null
        _isDialogAlreadyMarked.value = false
        _authState.value = AuthState(
            isAuthenticated = true,
            employeeId = empId,
            loginTime = System.currentTimeMillis()
        )
        socketClient.addLog(
            LogDirection.INFO,
            "Customer authentication successful for Employee ID: $empId"
        )
    }

    fun logout() {
        _activeDialogRecord.value = null
        _serverErrorMessage.value = null
        _isDialogAlreadyMarked.value = false
        _isProcessingTimeIn.value = false
        _isProcessingTimeOut.value = false
        repository.clearCurrentSession()
        _authState.value = AuthState(
            isAuthenticated = false,
            employeeId = ""
        )
        socketClient.addLog(
            LogDirection.INFO,
            "User session logged out. Session refreshed for next login."
        )
    }

    fun dismissAuth() {
        _authState.value = _authState.value.copy(isAuthenticated = true)
        socketClient.addLog(
            LogDirection.INFO,
            "Login dialog dismissed by user"
        )
    }

    fun updateProfile(profile: EmployeeProfile) {
        repository.updateProfile(profile)
        socketClient.addLog(
            LogDirection.INFO,
            "Employee Profile updated: ${profile.name} (ID: ${profile.employeeId})"
        )
    }

    fun updateSocketConfig(config: SocketConfig) {
        repository.updateSocketConfig(config)
    }
}
