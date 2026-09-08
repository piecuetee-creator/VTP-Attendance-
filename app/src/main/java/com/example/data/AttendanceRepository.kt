package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AttendanceRecord
import com.example.model.AttendanceType
import com.example.model.EmployeeProfile
import com.example.model.SocketConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("presence_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
    private val dayKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    private val _employeeProfile = MutableStateFlow(
        EmployeeProfile(
            employeeId = prefs.getString("emp_id", "9771") ?: "9771",
            name = prefs.getString("emp_name", "Saad Ali Hafiz") ?: "Saad Ali Hafiz",
            designation = prefs.getString("emp_desig", "Manager - Data Analytics & BI")
                ?: "Manager - Data Analytics & BI",
            location = prefs.getString("emp_loc", "999 - Karim Chamber Offices, Karachi")
                ?: "999 - Karim Chamber Offices, Karachi"
        )
    )
    val employeeProfile = _employeeProfile.asStateFlow()

    private val _socketConfig = MutableStateFlow(
        SocketConfig(
            wsUrl = prefs.getString("ws_url", "ws://avl.vtps.org:5200") ?: "ws://avl.vtps.org:5200",
            tcpHost = prefs.getString("tcp_host", "avl.vtps.org") ?: "avl.vtps.org",
            tcpPort = prefs.getInt("tcp_port", 5200),
            imei = prefs.getString("imei", "990003333257875") ?: "990003333257875",
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
        rxHex: String?
    ): AttendanceRecord {
        val now = System.currentTimeMillis()
        val formattedDate = dateFormat.format(Date(now))
        val profile = _employeeProfile.value
        val config = _socketConfig.value
        val today = dayKeyFormat.format(Date(now))
        val prefix = "${type.name}_$today"

        prefs.edit().apply {
            putLong("${prefix}_time", now)
            putString("${prefix}_formatted", formattedDate)
            putFloat("${prefix}_lat", lat.toFloat())
            putFloat("${prefix}_lon", lon.toFloat())
            putString("${prefix}_loc", profile.location)
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
            locationName = profile.location,
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

    fun updateProfile(profile: EmployeeProfile) {
        _employeeProfile.value = profile
        prefs.edit().apply {
            putString("emp_id", profile.employeeId)
            putString("emp_name", profile.name)
            putString("emp_desig", profile.designation)
            putString("emp_loc", profile.location)
            apply()
        }
    }

    fun updateSocketConfig(config: SocketConfig) {
        _socketConfig.value = config
        prefs.edit().apply {
            putString("ws_url", config.wsUrl)
            putString("tcp_host", config.tcpHost)
            putInt("tcp_port", config.tcpPort)
            putString("imei", config.imei)
            putBoolean("use_ws", config.useWebSocket)
            apply()
        }
    }
}
