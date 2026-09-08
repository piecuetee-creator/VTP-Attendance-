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
    val employeeId: String = "9771",
    val name: String = "Saad Ali Hafiz",
    val designation: String = "Manager - Data Analytics & BI",
    val location: String = "999 - Karim Chamber Offices, Karachi"
)

data class SocketConfig(
    val wsUrl: String = "ws://avl.vtps.org:5200",
    val tcpHost: String = "avl.vtps.org",
    val tcpPort: Int = 5200,
    val imei: String = "990003333257875",
    val useWebSocket: Boolean = true
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
    val rxHex: String? = null
)
