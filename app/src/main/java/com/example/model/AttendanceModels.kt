package com.example.model

enum class AttendanceType {
    TIME_IN,
    TIME_OUT
}

enum class LogDirection {
    INFO,
    TX,
    RX,
    ERROR
}

data class SocketLogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: String,
    val direction: LogDirection,
    val message: String,
    val hexData: String? = null
)

data class EmployeeProfile(
    val employeeId: String = "",
    val name: String = "",
    val designation: String = "",
    val location: String = "Karim Chamber Offices, Karachi",
    val imei: String = "",
    val companyCode: String = "",
    val employeeCode: String = "",
    val serverDigits: String = "01"
)

data class SocketConfig(
    val wsUrl: String = "ws://avl.vtps.org:5200",
    val tcpHost: String = "avl.vtps.org",
    val tcpPort: Int = 5200,
    val imei: String = "990021001045201",
    val useWebSocket: Boolean = true,
    val serverDigits: String = "01"
)

data class AttendanceRecord(
    val id: Long = System.currentTimeMillis(),
    val type: AttendanceType,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDateTime: String,
    val latitude: Double,
    val longitude: Double,
    val employeeId: String,
    val employeeName: String,
    val locationName: String,
    val imei: String,
    val status: String,
    val txHex: String? = null,
    val rxHex: String? = null,
    val speedKmh: Float = 0f,
    val courseAngle: Float = 0f
)
