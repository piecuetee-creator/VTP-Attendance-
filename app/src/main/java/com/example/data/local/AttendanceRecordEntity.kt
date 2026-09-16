package com.example.data.local

data class AttendanceRecordEntity(
    val id: Long = 0,
    val type: String, // "TIME_IN" or "TIME_OUT"
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDateTime: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float = 0f,
    val courseAngle: Float = 0f,
    val satellitesCount: Int = 11,
    val altitudeMeters: Double = 15.0,
    val companyCode: String = "",
    val employeeCode: String = "",
    val employeeName: String = "",
    val locationName: String = "",
    val imei: String = "",
    val isSynced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncError: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
