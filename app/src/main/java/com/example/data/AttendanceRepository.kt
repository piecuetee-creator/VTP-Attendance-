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

    // Determine initial 15-digit IMEI following the internal 99002 pattern
    private val savedCompanyCode = prefs.getString("emp_company_code", "") ?: ""
    private val savedEmployeeCode = prefs.getString("emp_code", "") ?: ""
    private val initialDeviceImei = run {
        val comp = savedCompanyCode.ifBlank { "1001" }
        val emp = savedEmployeeCode.ifBlank { "000452" }
        val raw = prefs.getString("imei", null)
        if (raw != null && raw.startsWith("99002") && raw.length == 15) {
            raw
        } else {
            val newlyBuilt = DeviceInfoManager.buildVtpImei(comp, emp, context)
            prefs.edit().putString("imei", newlyBuilt).putString("emp_imei", newlyBuilt).apply()
            newlyBuilt
        }
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

    private val _lastTimeIn = MutableStateFlow<AttendanceRecord?>(
        loadSavedRecord(AttendanceType.TIME_IN, savedCompanyCode, savedEmployeeCode)
    )
    val lastTimeIn = _lastTimeIn.asStateFlow()

    private val _lastTimeOut = MutableStateFlow<AttendanceRecord?>(
        loadSavedRecord(AttendanceType.TIME_OUT, savedCompanyCode, savedEmployeeCode)
    )
    val lastTimeOut = _lastTimeOut.asStateFlow()

    fun loadSavedRecord(
        type: AttendanceType,
        companyCode: String = _employeeProfile.value.companyCode,
        employeeCode: String = _employeeProfile.value.employeeCode
    ): AttendanceRecord? {
        if (employeeCode.isBlank()) return null
        val today = dayKeyFormat.format(Date())
        val scopedPrefix = "${type.name}_${today}_${companyCode}_${employeeCode}"

        val prefix = if (prefs.contains("${scopedPrefix}_time")) {
            scopedPrefix
        } else {
            // Check legacy key for backward compatibility, but strictly verify that the record belongs to this employee
            val legacyPrefix = "${type.name}_$today"
            val savedEmp = prefs.getString("${legacyPrefix}_empId", "") ?: ""
            val savedComp = prefs.getString("${legacyPrefix}_comp", "") ?: ""
            if (prefs.contains("${legacyPrefix}_time") &&
                savedEmp == employeeCode &&
                (savedComp.isBlank() || savedComp == companyCode)
            ) {
                legacyPrefix
            } else {
                return null
            }
        }

        val time = prefs.getLong("${prefix}_time", 0L)
        val formatted = prefs.getString("${prefix}_formatted", "") ?: ""
        val lat = prefs.getFloat("${prefix}_lat", 0f).toDouble()
        val lon = prefs.getFloat("${prefix}_lon", 0f).toDouble()
        val loc = prefs.getString("${prefix}_loc", "") ?: ""
        val empId = prefs.getString("${prefix}_empId", "") ?: employeeCode
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

    fun clearCurrentSession() {
        _lastTimeIn.value = null
        _lastTimeOut.value = null
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
        val scopedPrefix = "${type.name}_${today}_${profile.companyCode}_${profile.employeeCode}"
        val effectiveLocation = locationNameOverride?.takeIf { it.isNotBlank() } ?: profile.location

        prefs.edit().apply {
            putLong("${scopedPrefix}_time", now)
            putString("${scopedPrefix}_formatted", formattedDate)
            putFloat("${scopedPrefix}_lat", lat.toFloat())
            putFloat("${scopedPrefix}_lon", lon.toFloat())
            putString("${scopedPrefix}_loc", effectiveLocation)
            putString("${scopedPrefix}_empId", profile.employeeCode)
            putString("${scopedPrefix}_comp", profile.companyCode)
            putString("${scopedPrefix}_empName", profile.name)
            putString("${scopedPrefix}_imei", config.imei)
            apply()
        }

        val record = AttendanceRecord(
            type = type,
            timestamp = now,
            formattedDateTime = formattedDate,
            latitude = lat,
            longitude = lon,
            employeeId = profile.employeeCode,
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
        if (prefs.getBoolean("profile_fixed", false)) {
            // Location is fixed from login - do not alter
            return
        }
        if (newLocation.isNotBlank() && _employeeProfile.value.location != newLocation) {
            updateProfile(_employeeProfile.value.copy(location = newLocation))
        }
    }

    fun resetBiometric(
        companyCode: String = _employeeProfile.value.companyCode,
        employeeCode: String = _employeeProfile.value.employeeCode
    ) {
        val today = dayKeyFormat.format(Date())
        val inPrefix = "${AttendanceType.TIME_IN.name}_${today}_${companyCode}_${employeeCode}"
        val outPrefix = "${AttendanceType.TIME_OUT.name}_${today}_${companyCode}_${employeeCode}"
        val legacyIn = "${AttendanceType.TIME_IN.name}_$today"
        val legacyOut = "${AttendanceType.TIME_OUT.name}_$today"

        prefs.edit().apply {
            remove("${inPrefix}_time")
            remove("${inPrefix}_formatted")
            remove("${outPrefix}_time")
            remove("${outPrefix}_formatted")
            remove("${legacyIn}_time")
            remove("${legacyIn}_formatted")
            remove("${legacyOut}_time")
            remove("${legacyOut}_formatted")
            apply()
        }
        _lastTimeIn.value = null
        _lastTimeOut.value = null
    }

    fun loginWithDetails(
        companyCode: String,
        employeeCode: String,
        name: String = "",
        designation: String = "",
        location: String = ""
    ) {
        val cleanComp = companyCode.filter { it.isDigit() }.let {
            if (it.length > 4) it.takeLast(4) else it.padStart(4, '0')
        }
        val cleanEmp = employeeCode.filter { it.isDigit() }.let {
            if (it.length > 6) it.takeLast(6) else it.padStart(6, '0')
        }
        val newImei = DeviceInfoManager.buildVtpImei(cleanComp, cleanEmp, context)

        val current = _employeeProfile.value
        val isDifferentUser = (current.companyCode != cleanComp || current.employeeCode != cleanEmp)

        val effectiveName = if (name.isNotBlank()) {
            name.trim()
        } else if (isDifferentUser) {
            ""
        } else {
            current.name
        }

        val effectiveDesig = if (designation.isNotBlank()) {
            designation.trim()
        } else if (isDifferentUser) {
            ""
        } else {
            current.designation
        }

        val effectiveLoc = if (location.isNotBlank()) {
            location.trim()
        } else if (isDifferentUser) {
            "Karim Chamber Offices, Karachi"
        } else {
            current.location
        }

        val updated = current.copy(
            companyCode = cleanComp,
            employeeCode = cleanEmp,
            employeeId = cleanEmp,
            name = effectiveName,
            designation = effectiveDesig,
            location = effectiveLoc,
            imei = newImei
        )
        _employeeProfile.value = updated
        _socketConfig.value = _socketConfig.value.copy(imei = newImei)

        prefs.edit().apply {
            putString("emp_company_code", cleanComp)
            putString("emp_code", cleanEmp)
            putString("emp_id", cleanEmp)
            putString("emp_name", effectiveName)
            putString("emp_desig", effectiveDesig)
            putString("emp_loc", effectiveLoc)
            putString("emp_imei", newImei)
            putString("imei", newImei)
            putBoolean("profile_fixed", true)
            apply()
        }

        // Refreshed session for the logged in user:
        // Only load attendance belonging specifically to this companyCode + employeeCode
        _lastTimeIn.value = loadSavedRecord(AttendanceType.TIME_IN, cleanComp, cleanEmp)
        _lastTimeOut.value = loadSavedRecord(AttendanceType.TIME_OUT, cleanComp, cleanEmp)
    }

    fun loginWithCodes(companyCode: String, employeeCode: String) {
        loginWithDetails(companyCode, employeeCode)
    }

    fun isProfileFixed(): Boolean {
        return prefs.getBoolean("profile_fixed", false)
    }

    fun updateProfile(profile: EmployeeProfile) {
        val isFixed = isProfileFixed()
        val current = _employeeProfile.value
        val sanitizedImei = if (profile.imei.isNotBlank()) DeviceInfoManager.sanitizeImei(profile.imei) else _socketConfig.value.imei
        val updatedProfile = if (isFixed) {
            // Profile details cannot be changed from settings once fixed
            current.copy(imei = sanitizedImei)
        } else {
            profile.copy(imei = sanitizedImei)
        }
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
