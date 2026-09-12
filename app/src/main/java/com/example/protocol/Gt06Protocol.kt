package com.example.protocol

import java.util.Calendar
import java.util.TimeZone

/**
 * GT06 GPS Tracker Protocol Implementation
 * Encodes Login (0x01) and Location (0x12) packets with ITU-T CRC-16 / X.25 error check.
 */
object Gt06Protocol {

    // Precomputed CRC-16 / X-25 (ITU-T V.41 HDLC FCS) lookup table (polynomial 0x8408 reflected)
    private val CRC_TABLE = intArrayOf(
        0x0000, 0x1189, 0x2312, 0x329B, 0x4624, 0x57AD, 0x6536, 0x74BF,
        0x8C48, 0x9DC1, 0xAF5A, 0xBED3, 0xCA6C, 0xDBE5, 0xE97E, 0xF8F7,
        0x1081, 0x0108, 0x3393, 0x221A, 0x56A5, 0x472C, 0x75B7, 0x643E,
        0x9CC9, 0x8D40, 0xBFDB, 0xAE52, 0xDAED, 0xCB64, 0xF9FF, 0xE876,
        0x2102, 0x308B, 0x0210, 0x1399, 0x6726, 0x76AF, 0x4434, 0x55BD,
        0xAD4A, 0xBCC3, 0x8E58, 0x9FD1, 0xEB6E, 0xFAE7, 0xC87C, 0xD9F5,
        0x3183, 0x200A, 0x1291, 0x0318, 0x77A7, 0x662E, 0x54B5, 0x453C,
        0xBDCB, 0xAC42, 0x9ED9, 0x8F50, 0xFBEF, 0xEA66, 0xD8FD, 0xC974,
        0x4204, 0x538D, 0x6116, 0x709F, 0x0420, 0x15A9, 0x2732, 0x36BB,
        0xCE4C, 0xDFC5, 0xED5E, 0xFCD7, 0x8868, 0x99E1, 0xAB7A, 0xBAF3,
        0x5285, 0x430C, 0x7197, 0x601E, 0x14A1, 0x0528, 0x37B3, 0x263A,
        0xDECD, 0xCF44, 0xFDDF, 0xEC56, 0x98E9, 0x8960, 0xBBFB, 0xAA72,
        0x6306, 0x728F, 0x4014, 0x519D, 0x2522, 0x34AB, 0x0630, 0x17B9,
        0xEF4E, 0xFEC7, 0xCC5C, 0xDDD5, 0xA96A, 0xB8E3, 0x8A78, 0x9BF1,
        0x7387, 0x620E, 0x5095, 0x411C, 0x35A3, 0x242A, 0x16B1, 0x0738,
        0xFFCF, 0xEE46, 0xDCDD, 0xCD54, 0xB9EB, 0xA862, 0x9AF9, 0x8B70,
        0x8408, 0x9581, 0xA71A, 0xB693, 0xC22C, 0xD3A5, 0xE13E, 0xF0B7,
        0x0840, 0x19C9, 0x2B52, 0x3ADB, 0x4E64, 0x5FED, 0x6D76, 0x7CFF,
        0x9489, 0x8500, 0xB79B, 0xA612, 0xD2AD, 0xC324, 0xF1BF, 0xE036,
        0x18C1, 0x0948, 0x3BD3, 0x2A5A, 0x5EE5, 0x4F6C, 0x7DF7, 0x6C7E,
        0xA50A, 0xB483, 0x8618, 0x9791, 0xE32E, 0xF2A7, 0xC03C, 0xD1B5,
        0x2942, 0x38CB, 0x0A50, 0x1BD9, 0x6F66, 0x7EEF, 0x4C74, 0x5DFD,
        0xB58B, 0xA402, 0x9699, 0x8710, 0xF3A9, 0xE220, 0xD0BB, 0xC132,
        0x39C3, 0x284A, 0x1AD1, 0x0B58, 0x7FE7, 0x6E6E, 0x5CF5, 0x4D7C,
        0xC60C, 0xD785, 0xE51E, 0xF497, 0x8028, 0x91A1, 0xA33A, 0xB2B3,
        0x4A44, 0x5BCD, 0x6956, 0x78DF, 0x0C60, 0x1DE9, 0x2F72, 0x3EFB,
        0xD68D, 0xC704, 0xF59F, 0xE416, 0x90A9, 0x8120, 0xB3BB, 0xA232,
        0x5AC5, 0x4B4C, 0x79D7, 0x685E, 0x1CE1, 0x0D68, 0x3FF3, 0x2E7A,
        0xE70E, 0xF687, 0xC41C, 0xD595, 0xA12A, 0xB0A3, 0x8238, 0x93B1,
        0x6B46, 0x7ACF, 0x4854, 0x59DD, 0x2D62, 0x3CEB, 0x0E70, 0x1FF9,
        0xF78F, 0xE606, 0xD49D, 0xC514, 0xB1AB, 0xA022, 0x92B9, 0x8330,
        0x7BC7, 0x6A4E, 0x58D5, 0x495C, 0x3DE3, 0x2C6A, 0x1EF1, 0x0F78
    )

    /**
     * Computes the CRC-16 checksum for the specified range of bytes according to GT06 specs.
     */
    fun crc16(data: ByteArray, offset: Int, length: Int): Int {
        var fcs = 0xFFFF
        for (i in offset until offset + length) {
            val b = data[i].toInt() and 0xFF
            fcs = (fcs ushr 8) xor CRC_TABLE[(fcs xor b) and 0xFF]
        }
        return (fcs xor 0xFFFF) and 0xFFFF
    }

    /**
     * Converts a 15-digit IMEI to 8-byte BCD array.
     */
    fun imeiToBcd(imei: String): ByteArray {
        val cleanImei = imei.filter { it.isDigit() }
        // Pad with leading 0 if odd number of digits (15 digits -> 16 digits)
        val padded = if (cleanImei.length % 2 != 0) "0$cleanImei" else cleanImei
        val bcd = ByteArray(8)
        for (i in 0 until 8) {
            val start = i * 2
            if (start + 2 <= padded.length) {
                val hexStr = padded.substring(start, start + 2)
                bcd[i] = hexStr.toIntOrNull(16)?.toByte() ?: 0
            } else {
                bcd[i] = 0
            }
        }
        return bcd
    }

    /**
     * Builds a GT06 Login Packet (0x01).
     * Total length: 18 bytes
     */
    fun buildLoginPacket(imei: String, serialNo: Int): ByteArray {
        val packet = ByteArray(18)
        // Start bits
        packet[0] = 0x78.toByte()
        packet[1] = 0x78.toByte()
        // Length (0x0D = 13 bytes: proto 1 + IMEI 8 + serial 2 + CRC 2)
        packet[2] = 0x0D.toByte()
        // Protocol: 0x01 (Login)
        packet[3] = 0x01.toByte()

        // 8 bytes BCD IMEI
        val bcdImei = imeiToBcd(imei)
        System.arraycopy(bcdImei, 0, packet, 4, 8)

        // Information serial number (2 bytes)
        packet[12] = ((serialNo ushr 8) and 0xFF).toByte()
        packet[13] = (serialNo and 0xFF).toByte()

        // CRC-16 (computed from byte 2 to 13, length 12 bytes)
        val crc = crc16(packet, 2, 12)
        packet[14] = ((crc ushr 8) and 0xFF).toByte()
        packet[15] = (crc and 0xFF).toByte()

        // Stop bits
        packet[16] = 0x0D.toByte()
        packet[17] = 0x0A.toByte()

        return packet
    }

    /**
     * Builds a GT06 GPS Location Packet (0x12).
     * @param lat Latitude in decimal degrees (e.g. 24.8607)
     * @param lon Longitude in decimal degrees (e.g. 67.0011)
     * @param isIgnitionOn true for Check In (ACC High), false for Check Out (ACC Low)
     * @param serialNo Packet sequence number
     * Builds a GT06 GPS Location Data Packet (Protocol 0x12).
     * Incorporates real speed (km/h) and heading angle (0-360 degrees) with standard GT06 status flags.
     *
     * @param lat Latitude in decimal degrees
     * @param lon Longitude in decimal degrees
     * @param isIgnitionOn Boolean indicating ACC / attendance trigger state
     * @param serialNo Packet sequence number
     * @param timestampMs Epoch timestamp in milliseconds
     * @param speedKmh Real speed in km/h (0 to 255)
     * @param courseAngle Direction heading in degrees (0.0 to 360.0)
     * @param satellitesCount Visible GPS satellite count
     * @param altitudeMeters Altitude in meters
     */
    fun buildLocationPacket(
        lat: Double,
        lon: Double,
        isIgnitionOn: Boolean,
        serialNo: Int,
        timestampMs: Long = System.currentTimeMillis(),
        speedKmh: Float = 0f,
        courseAngle: Float = 0f,
        satellitesCount: Int = 11,
        altitudeMeters: Double = 0.0
    ): ByteArray {
        val totalLength = 36 // 2 start + 1 len + 1 proto + 6 time + 1 gps + 4 lat + 4 lon + 1 spd + 2 course + 8 lbs + 2 serial + 2 crc + 2 stop
        val packet = ByteArray(36)

        // Start bits
        packet[0] = 0x78.toByte()
        packet[1] = 0x78.toByte()

        // Packet length: from byte 3 to byte 31 (29 bytes) + 2 crc = 31 bytes (0x1F)
        packet[2] = 0x1F.toByte()

        // Protocol number: 0x12 (GPS Location)
        packet[3] = 0x12.toByte()

        // Date Time: 6 bytes (YY MM DD HH mm ss in UTC)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = timestampMs
        packet[4] = ((cal.get(Calendar.YEAR) % 100) and 0xFF).toByte()
        packet[5] = ((cal.get(Calendar.MONTH) + 1) and 0xFF).toByte()
        packet[6] = (cal.get(Calendar.DAY_OF_MONTH) and 0xFF).toByte()
        packet[7] = (cal.get(Calendar.HOUR_OF_DAY) and 0xFF).toByte()
        packet[8] = (cal.get(Calendar.MINUTE) and 0xFF).toByte()
        packet[9] = (cal.get(Calendar.SECOND) and 0xFF).toByte()

        // GPS Info length (0xC for 12 bytes) & Satellites count (low nibble 0x0B = 11 satellites)
        val clampedSats = satellitesCount.coerceIn(1, 15)
        packet[10] = (0xC0 or (clampedSats and 0x0F)).toByte()

        // Latitude: degrees * 1,800,000 (Big-endian 4 bytes)
        val latUnits = (Math.abs(lat) * 1800000.0).toLong()
        packet[11] = ((latUnits ushr 24) and 0xFF).toByte()
        packet[12] = ((latUnits ushr 16) and 0xFF).toByte()
        packet[13] = ((latUnits ushr 8) and 0xFF).toByte()
        packet[14] = (latUnits and 0xFF).toByte()

        // Longitude: degrees * 1,800,000 (Big-endian 4 bytes)
        val lonUnits = (Math.abs(lon) * 1800000.0).toLong()
        packet[15] = ((lonUnits ushr 24) and 0xFF).toByte()
        packet[16] = ((lonUnits ushr 16) and 0xFF).toByte()
        packet[17] = ((lonUnits ushr 8) and 0xFF).toByte()
        packet[18] = (lonUnits and 0xFF).toByte()

        // Speed in km/h (1 byte: 0 to 255 km/h)
        val clampedSpeed = speedKmh.coerceIn(0f, 255f).toInt()
        packet[19] = (clampedSpeed and 0xFF).toByte()

        // Course & Status flags (2 bytes / 16 bits):
        // Bit 14: GPS tracked / positioned (1)
        // Bit 12: GPS valid (1)
        // Bit 11: 1 if West longitude, 0 if East longitude
        // Bit 10: 1 if South latitude, 0 if North latitude
        // Bits 0..9: Course Angle in degrees (0 to 359)
        var statusFlags = 0x5400 // GPS tracked (0x4000) + GPS valid (0x1000) + North (0x0400)
        if (lon < 0) statusFlags = statusFlags or 0x0800 // West longitude
        if (lat < 0) statusFlags = statusFlags and 0x0400.inv() // South latitude

        val clampedAngle = ((courseAngle % 360f + 360f) % 360f).toInt() and 0x03FF
        val courseFlags = (statusFlags and 0xFC00) or clampedAngle

        packet[20] = ((courseFlags ushr 8) and 0xFF).toByte()
        packet[21] = (courseFlags and 0xFF).toByte()

        // LBS information (Cell tower info: MCC 2 bytes, MNC 1 byte, LAC 2 bytes, CellID 3 bytes)
        packet[22] = 0x01.toByte() // MCC high (410 or 424)
        packet[23] = 0xCC.toByte() // MCC low
        packet[24] = 0x01.toByte() // MNC
        packet[25] = 0x27.toByte() // LAC high
        packet[26] = 0x23.toByte() // LAC low
        packet[27] = 0x00.toByte() // Cell ID byte 1
        packet[28] = 0x1A.toByte() // Cell ID byte 2
        packet[29] = 0x6E.toByte() // Cell ID byte 3

        // Information serial number (2 bytes)
        packet[30] = ((serialNo ushr 8) and 0xFF).toByte()
        packet[31] = (serialNo and 0xFF).toByte()

        // CRC-16 (from byte 2 to byte 31, length 30 bytes)
        val crc = crc16(packet, 2, 30)
        packet[32] = ((crc ushr 8) and 0xFF).toByte()
        packet[33] = (crc and 0xFF).toByte()

        // Stop bits
        packet[34] = 0x0D.toByte()
        packet[35] = 0x0A.toByte()

        return packet
    }

    data class ParsedLocation(
        val latitude: Double,
        val longitude: Double,
        val speedKmh: Int,
        val courseAngle: Int,
        val cardinalDirection: String,
        val satellites: Int,
        val isGpsTracked: Boolean,
        val isNorth: Boolean,
        val isEast: Boolean,
        val serialNo: Int,
        val utcTime: String
    )

    fun parseLocationPacket(packet: ByteArray): ParsedLocation? {
        if (packet.size < 36 || packet[3] != 0x12.toByte()) return null
        return try {
            val year = 2000 + (packet[4].toInt() and 0xFF)
            val month = packet[5].toInt() and 0xFF
            val day = packet[6].toInt() and 0xFF
            val hour = packet[7].toInt() and 0xFF
            val min = packet[8].toInt() and 0xFF
            val sec = packet[9].toInt() and 0xFF
            val utcStr = String.format(java.util.Locale.US, "%04d-%02d-%02d %02d:%02d:%02d UTC", year, month, day, hour, min, sec)

            val sats = packet[10].toInt() and 0x0F

            val rawLat = ((packet[11].toLong() and 0xFF) shl 24) or
                    ((packet[12].toLong() and 0xFF) shl 16) or
                    ((packet[13].toLong() and 0xFF) shl 8) or
                    (packet[14].toLong() and 0xFF)
            val lat = rawLat / 1800000.0

            val rawLon = ((packet[15].toLong() and 0xFF) shl 24) or
                    ((packet[16].toLong() and 0xFF) shl 16) or
                    ((packet[17].toLong() and 0xFF) shl 8) or
                    (packet[18].toLong() and 0xFF)
            val lon = rawLon / 1800000.0

            val speed = packet[19].toInt() and 0xFF

            val courseHigh = packet[20].toInt() and 0xFF
            val courseLow = packet[21].toInt() and 0xFF
            val courseFlags = (courseHigh shl 8) or courseLow
            val angle = courseFlags and 0x03FF
            val isWest = (courseFlags and 0x0800) != 0
            val isNorth = (courseFlags and 0x0400) != 0
            val isTracked = (courseFlags and 0x4000) != 0

            val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
            val cardIdx = (((angle % 360 + 360) % 360 + 11.25f) / 22.5f).toInt() % 16

            val serial = ((packet[30].toInt() and 0xFF) shl 8) or (packet[31].toInt() and 0xFF)

            ParsedLocation(
                latitude = if (isNorth) lat else -lat,
                longitude = if (isWest) -lon else lon,
                speedKmh = speed,
                courseAngle = angle,
                cardinalDirection = directions[cardIdx],
                satellites = sats,
                isGpsTracked = isTracked,
                isNorth = isNorth,
                isEast = !isWest,
                serialNo = serial,
                utcTime = utcStr
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Converts a byte array to formatted hex string (e.g. "78 78 0D 01 ...").
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { String.format("%02X", it) }
    }

    /**
     * Parses a GT06 ACK packet if valid.
     */
    fun isAckPacket(bytes: ByteArray, expectedProtocol: Byte): Boolean {
        if (bytes.size < 10) return false
        if (bytes[0] != 0x78.toByte() || bytes[1] != 0x78.toByte()) return false
        // Protocol byte is at index 3
        return bytes[3] == expectedProtocol
    }
}
