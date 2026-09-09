package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import java.security.MessageDigest

object DeviceInfoManager {

    /**
     * Attempts to retrieve the real phone IMEI or a persistent 15-digit hardware device identifier.
     */
    @SuppressLint("HardwareIds")
    fun getDeviceImei(context: Context): String {
        // 1. Check if READ_PHONE_STATE is granted and attempt to read TelephonyManager IMEI
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                if (telephonyManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val imei = try { telephonyManager.imei } catch (_: SecurityException) { null }
                        if (!imei.isNullOrBlank()) {
                            val digits = imei.filter { it.isDigit() }
                            if (digits.length >= 14) return digits.take(15).padEnd(15, '0')
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val devId = try { telephonyManager.deviceId } catch (_: SecurityException) { null }
                        if (!devId.isNullOrBlank()) {
                            val digits = devId.filter { it.isDigit() }
                            if (digits.length >= 14) return digits.take(15).padEnd(15, '0')
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore security or vendor exceptions
            }
        }

        // 2. Derive deterministic 15-digit hardware IMEI from Android ID + Device Hardware Info
        return generateDeterministicImei(context)
    }

    /**
     * Generates a persistent 15-digit numeric IMEI starting with 86 (standard TAC) based on the unique hardware ANDROID_ID.
     */
    @SuppressLint("HardwareIds")
    fun generateDeterministicImei(context: Context): String {
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "device_id"
        } catch (_: Exception) {
            "device_id"
        }

        val seed = "${Build.MANUFACTURER}_${Build.MODEL}_$androidId"
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(seed.toByteArray(Charsets.UTF_8))
            val digits = StringBuilder("86") // Standard 3GPP TAC prefix
            for (b in digest) {
                val num = (b.toInt() and 0xFF) % 10
                digits.append(num)
                if (digits.length == 15) break
            }
            while (digits.length < 15) {
                digits.append("0")
            }
            digits.toString().take(15)
        } catch (_: Exception) {
            "860003333257875"
        }
    }

    /**
     * Builds a 15-digit IMEI adhering strictly to the internal VTP specification:
     * - "99"     : VTP (fix) (2 digits)
     * - "002"    : Product (fix) (3 digits)
     * - "xxxx"   : Company code (4 digits, numeric, e.g. 1001)
     * - "xxxxxx" : Employee code (6 digits, numeric, padded with leading zeros, e.g. 001 -> 000001)
     * Pattern: 99002xxxxxxxxxx (Total = 2 + 3 + 4 + 6 = 15 digits)
     */
    fun buildVtpImei(
        companyCode: String,
        employeeCode: String,
        @Suppress("UNUSED_PARAMETER") context: Context? = null
    ): String {
        val cleanComp = companyCode.filter { it.isDigit() }.let {
            if (it.length > 4) it.takeLast(4) else it.padStart(4, '0')
        }
        val cleanEmp = employeeCode.filter { it.isDigit() }.let {
            if (it.length > 6) it.takeLast(6) else it.padStart(6, '0')
        }
        return "99002$cleanComp$cleanEmp"
    }

    fun getSavedRandomSuffix(context: Context): String {
        val prefs = context.getSharedPreferences("vtp_device_prefs", Context.MODE_PRIVATE)
        var suffix = prefs.getString("vtp_imei_rand_suffix", null)
        if (suffix.isNullOrBlank() || suffix.length != 3 || !suffix.all { it.isDigit() }) {
            val generated = (100..999).random().toString()
            prefs.edit().putString("vtp_imei_rand_suffix", generated).apply()
            suffix = generated
        }
        return suffix
    }

    /**
     * Cleans and validates a 15-digit IMEI string.
     */
    fun sanitizeImei(input: String): String {
        val digits = input.filter { it.isDigit() }
        return if (digits.length >= 15) digits.take(15) else digits.padEnd(15, '0')
    }
}
