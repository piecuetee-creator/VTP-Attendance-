package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AttendanceDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val _unsyncedCountFlow = MutableStateFlow(0)
    val unsyncedCountFlow: StateFlow<Int> = _unsyncedCountFlow.asStateFlow()

    init {
        refreshUnsyncedCount()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                formattedDateTime TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                speedKmh REAL NOT NULL,
                courseAngle REAL NOT NULL,
                satellitesCount INTEGER NOT NULL,
                altitudeMeters REAL NOT NULL,
                companyCode TEXT NOT NULL,
                employeeCode TEXT NOT NULL,
                employeeName TEXT NOT NULL,
                locationName TEXT NOT NULL,
                imei TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                syncAttempts INTEGER NOT NULL DEFAULT 0,
                lastSyncError TEXT,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertRecord(record: AttendanceRecordEntity): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("type", record.type)
            put("timestamp", record.timestamp)
            put("formattedDateTime", record.formattedDateTime)
            put("latitude", record.latitude)
            put("longitude", record.longitude)
            put("speedKmh", record.speedKmh)
            put("courseAngle", record.courseAngle)
            put("satellitesCount", record.satellitesCount)
            put("altitudeMeters", record.altitudeMeters)
            put("companyCode", record.companyCode)
            put("employeeCode", record.employeeCode)
            put("employeeName", record.employeeName)
            put("locationName", record.locationName)
            put("imei", record.imei)
            put("isSynced", if (record.isSynced) 1 else 0)
            put("syncAttempts", record.syncAttempts)
            put("lastSyncError", record.lastSyncError)
            put("createdAt", record.createdAt)
        }
        val id = db.insert(TABLE_NAME, null, values)
        refreshUnsyncedCount()
        return id
    }

    fun getUnsyncedRecords(): List<AttendanceRecordEntity> {
        val list = mutableListOf<AttendanceRecordEntity>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "isSynced = 0",
            null,
            null,
            null,
            "timestamp ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    AttendanceRecordEntity(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        type = it.getString(it.getColumnIndexOrThrow("type")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        formattedDateTime = it.getString(it.getColumnIndexOrThrow("formattedDateTime")),
                        latitude = it.getDouble(it.getColumnIndexOrThrow("latitude")),
                        longitude = it.getDouble(it.getColumnIndexOrThrow("longitude")),
                        speedKmh = it.getFloat(it.getColumnIndexOrThrow("speedKmh")),
                        courseAngle = it.getFloat(it.getColumnIndexOrThrow("courseAngle")),
                        satellitesCount = it.getInt(it.getColumnIndexOrThrow("satellitesCount")),
                        altitudeMeters = it.getDouble(it.getColumnIndexOrThrow("altitudeMeters")),
                        companyCode = it.getString(it.getColumnIndexOrThrow("companyCode")),
                        employeeCode = it.getString(it.getColumnIndexOrThrow("employeeCode")),
                        employeeName = it.getString(it.getColumnIndexOrThrow("employeeName")),
                        locationName = it.getString(it.getColumnIndexOrThrow("locationName")),
                        imei = it.getString(it.getColumnIndexOrThrow("imei")),
                        isSynced = it.getInt(it.getColumnIndexOrThrow("isSynced")) == 1,
                        syncAttempts = it.getInt(it.getColumnIndexOrThrow("syncAttempts")),
                        lastSyncError = it.getString(it.getColumnIndexOrThrow("lastSyncError")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("createdAt"))
                    )
                )
            }
        }
        return list
    }

    fun markSynced(id: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("isSynced", 1)
            putNull("lastSyncError")
        }
        db.update(TABLE_NAME, values, "id = ?", arrayOf(id.toString()))
        refreshUnsyncedCount()
    }

    fun markSyncFailed(id: Long, error: String) {
        val db = writableDatabase
        db.execSQL(
            "UPDATE $TABLE_NAME SET syncAttempts = syncAttempts + 1, lastSyncError = ? WHERE id = ?",
            arrayOf(error, id.toString())
        )
        refreshUnsyncedCount()
    }

    fun getUnsyncedCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME WHERE isSynced = 0", null)
        var count = 0
        cursor.use {
            if (it.moveToFirst()) {
                count = it.getInt(0)
            }
        }
        return count
    }

    fun refreshUnsyncedCount() {
        val count = getUnsyncedCount()
        _unsyncedCountFlow.value = count
    }

    companion object {
        const val DATABASE_NAME = "vtp_attendance_offline.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "offline_attendance"

        @Volatile
        private var INSTANCE: AttendanceDatabaseHelper? = null

        fun getInstance(context: Context): AttendanceDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = AttendanceDatabaseHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
