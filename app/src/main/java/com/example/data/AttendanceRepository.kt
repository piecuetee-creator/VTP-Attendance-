package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AttendanceRecord
import com.example.model.AttendanceType
import com.example.model.EmployeeProfile
import com.example.model.SocketConfig
import com.example.util.DeviceInfoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("presence_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
    private val dayKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    // Determine initial 15-digit IMEI following the 9902 formula if company/employee codes exist
    private val savedCompanyCode = prefs.getString("emp_company_code", "") ?: ""
    private val savedEmployeeCode = prefs.getString("emp_code", "") ?: ""
    private val initialDeviceImei = if (savedCompanyCode.isNotBlank() && savedEmployeeCode.isNotBlank()) {
        DeviceInfoManager.buildVtpImei(savedCompanyCode, savedEmployeeCode, context)
    } else {
        prefs.getString("imei", null) ?: DeviceInfoManager.buildVtpImei("1001", "0452", context)
    }

    private val _employeeProfile = MutableStateFlow(
        run {
            val rawName = prefs.getString("emp_name", "") ?: ""
            val rawId = prefs.getString("emp_id", savedEmployeeCode) ?: savedEmployeeCode
            val rawDesig = prefs.getString("emp_desig", "") ?: ""
            val rawLoc = prefs.getString("emp_loc", "") ?: ""
            val rawImei = prefs.getString("emp_imei", initialDeviceImei) ?: initialDeviceImei

            // Purge template placeholder values if they were saved in previous sessions
            val cleanName = if (rawName == "Saad Ali Hafiz") "" else rawName
            val cleanId = if (rawId == "9771") "" else rawId
            val cleanDesig = if (rawDesig == "Manager - Data Analytics & BI") "" else rawDesig
            val cleanLoc = if (rawLoc.isBlank() || rawLoc.startsWith("999")) "Karim Chamber Offices, Karachi" else rawLoc

            EmployeeProfile(
                employeeId = cleanId,
                name = cleanName,
                designation = cleanDesig,
                location = cleanLoc,
                imei = rawImei,
                companyCode = savedCompanyCode,
                employeeCode = savedEmployeeCode
            )
        }
    )
    val employeeProfile = _employeeProfile.asStateFlow()

    private val _socketConfig = MutableStateFlow(
        SocketConfig(
            wsUrl = prefs.getString("ws_url", "ws://avl.vtps.org:5200") ?: "ws://avl.vtps.org:5200",
            tcpHost = prefs.getString("tcp_host", "avl.vtps.org") ?: "avl.vtps.org",
            tcpPort = prefs.getInt("tcp_port", 5200),
            imei = initialDeviceImei,
            useWebSocket = prefs.getBoolean("use_ws", true)
        )
    )
    val socketConfig = _socketConfig.asStateFlow()

    private val _lastTimeIn = MutableStateFlow<AttendanceRecord?>(loadSavedRecord(AttendanceType.TIME_IN))
    val lastTimeIn = _lastTimeIn.asStateFlow()

    private val _lastTimeOut = MutableStateFlow<AttendanceRecord?>(loadSavedRecord(AttendanceType.TIME_OUT))
    val lastTimeOut = _lastTimeOut.asStateFlow()

    private fun loadSavedRecord(type: AttendanceType): AttendanceRecord? {
        val today = dayKeyFormat.format(Date())
        val prefix = "${type.name}_$today"
        if (!prefs.contains("${prefix}_time")) return null

        val time = prefs.getLong("${prefix}_time", 0L)
        val formatted = prefs.getString("${prefix}_formatted", "") ?: ""
        val lat = prefs.getFloat("${prefix}_lat", 0f).toDouble()
        val lon = prefs.getFloat("${prefix}_lon", 0f).toDouble()
        val loc = prefs.getString("${prefix}_loc", "") ?: ""
        val empId = prefs.getString("${prefix}_empId", "") ?: ""
        val empName = prefs.getString("${prefix}_empName", "") ?: ""
        val imei = prefs.getString("${prefix}_imei", "") ?: ""

        return AttendanceRecord(
            type = type,
            timestamp = time,
            formattedDateTime = formatted,
            latitude = lat,
            longitude = lon,
            employeeId = empId,
            employeeName = empName,
            locationName = loc,
            imei = imei,
            status = "Marked"
        )
    }

    fun saveAttendanceRecord(
        type: AttendanceType,
        lat: Double,
        lon: Double,
        txHex: String?,
        rxHex: String?,
        locationNameOverride: String? = null
    ): AttendanceRecord {
        val now = System.currentTimeMillis()
        val formattedDate = dateFormat.format(Date(now))
        val profile = _employeeProfile.value
        val config = _socketConfig.value
        val today = dayKeyFormat.format(Date(now))
        val prefix = "${type.name}_$today"
        val effectiveLocation = locationNameOverride?.takeIf { it.isNotBlank() } ?: profile.location

        prefs.edit().apply {
            putLong("${prefix}_time", now)
            putString("${prefix}_formatted", formattedDate)
            putFloat("${prefix}_lat", lat.toFloat())
            putFloat("${prefix}_lon", lon.toFloat())
            putString("${prefix}_loc", effectiveLocation)
            putString("${prefix}_empId", profile.employeeId)
            putString("${prefix}_empName", profile.name)
            putString("${prefix}_imei", config.imei)
            apply()
        }

        val record = AttendanceRecord(
            type = type,
            timestamp = now,
            formattedDateTime = formattedDate,
            latitude = lat,
            longitude = lon,
            employeeId = profile.employeeId,
            employeeName = profile.name,
            locationName = effectiveLocation,
            imei = config.imei,
            status = "Success",
            txHex = txHex,
            rxHex = rxHex
        )

        if (type == AttendanceType.TIME_IN) {
            _lastTimeIn.value = record
        } else {
            _lastTimeOut.value = record
        }

        return record
    }

    fun updateProfileLocation(newLocation: String) {
        if (newLocation.isNotBlank() && _employeeProfile.value.location != newLocation) {
            updateProfile(_employeeProfile.value.copy(location = newLocation))
        }
    }

    fun resetBiometric() {
        val today = dayKeyFormat.format(Date())
        prefs.edit().apply {
            remove("${AttendanceType.TIME_IN.name}_${today}_time")
            remove("${AttendanceType.TIME_OUT.name}_${today}_time")
            apply()
        }
        _lastTimeIn.value = null
        _lastTimeOut.value = null
    }

    fun loginWithCodes(companyCode: String, employeeCode: String) {
        val cleanComp = companyCode.filter { it.isDigit() }.padStart(4, '0').takeLast(4)
        val cleanEmp = employeeCode.filter { it.isDigit() }.padStart(4, '0').takeLast(4)
        val newImei = DeviceInfoManager.buildVtpImei(cleanComp, cleanEmp, context)

        val current = _employeeProfile.value
        val updated = current.copy(
            companyCode = cleanComp,
            employeeCode = cleanEmp,
            employeeId = cleanEmp,
            imei = newImei
        )
        _employeeProfile.value = updated
        _socketConfig.value = _socketConfig.value.copy(imei = newImei)

        prefs.edit().apply {
            putString("emp_company_code", cleanComp)
            putString("emp_code", cleanEmp)
            putString("emp_id", cleanEmp)
            putString("emp_imei", newImei)
            putString("imei", newImei)
            apply()
        }
    }

    fun updateProfile(profile: EmployeeProfile) {
        val sanitizedImei = if (profile.imei.isNotBlank()) DeviceInfoManager.sanitizeImei(profile.imei) else _socketConfig.value.imei
        val updatedProfile = profile.copy(imei = sanitizedImei)
        _employeeProfile.value = updatedProfile
        prefs.edit().apply {
            putString("emp_id", updatedProfile.employeeId)
            putString("emp_name", updatedProfile.name)
            putString("emp_desig", updatedProfile.designation)
            putString("emp_loc", updatedProfile.location)
            putString("emp_imei", updatedProfile.imei)
            putString("imei", updatedProfile.imei)
            apply()
        }
        if (_socketConfig.value.imei != sanitizedImei) {
            _socketConfig.value = _socketConfig.value.copy(imei = sanitizedImei)
        }
    }

    fun updateSocketConfig(config: SocketConfig) {
        val sanitizedImei = DeviceInfoManager.sanitizeImei(config.imei)
        val updatedConfig = config.copy(imei = sanitizedImei)
        _socketConfig.value = updatedConfig
        prefs.edit().apply {
            putString("ws_url", updatedConfig.wsUrl)
            putString("tcp_host", updatedConfig.tcpHost)
            putInt("tcp_port", updatedConfig.tcpPort)
            putString("imei", updatedConfig.imei)
            putString("emp_imei", updatedConfig.imei)
            putBoolean("use_ws", updatedConfig.useWebSocket)
            apply()
        }
        if (_employeeProfile.value.imei != sanitizedImei) {
            _employeeProfile.value = _employeeProfile.value.copy(imei = sanitizedImei)
        }
    }
}
