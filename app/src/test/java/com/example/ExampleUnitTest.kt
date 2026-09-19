package com.example

import com.example.protocol.Gt06Protocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun crc16_knownVector_isCorrect() {
        // Known vector: 0D 01 03 53 41 35 32 15 03 62 00 02 -> CRC is 0x2D06
        val data = byteArrayOf(
            0x0D.toByte(), 0x01.toByte(), 0x03.toByte(), 0x53.toByte(),
            0x41.toByte(), 0x35.toByte(), 0x32.toByte(), 0x15.toByte(),
            0x03.toByte(), 0x62.toByte(), 0x00.toByte(), 0x02.toByte()
        )
        val crc = Gt06Protocol.crc16(data, 0, data.size)
        assertEquals(0x2D06, crc)
    }

    @Test
    fun buildLoginPacket_structureIsValid() {
        val imei = "990003333257875"
        val packet = Gt06Protocol.buildLoginPacket(imei, 1)

        assertEquals(18, packet.size)
        // Start bits
        assertEquals(0x78.toByte(), packet[0])
        assertEquals(0x78.toByte(), packet[1])
        // Length
        assertEquals(0x0D.toByte(), packet[2])
        // Protocol
        assertEquals(0x01.toByte(), packet[3])
        // Stop bits
        assertEquals(0x0D.toByte(), packet[16])
        assertEquals(0x0A.toByte(), packet[17])
    }

    @Test
    fun buildLocationPacket_structureIsValid() {
        val packet = Gt06Protocol.buildLocationPacket(
            lat = 24.8607,
            lon = 67.0011,
            isIgnitionOn = true,
            serialNo = 2
        )

        assertEquals(36, packet.size)
        // Start bits
        assertEquals(0x78.toByte(), packet[0])
        assertEquals(0x78.toByte(), packet[1])
        // Length
        assertEquals(0x1F.toByte(), packet[2])
        // Protocol
        assertEquals(0x12.toByte(), packet[3])
        // Stop bits
        assertEquals(0x0D.toByte(), packet[34])
        assertEquals(0x0A.toByte(), packet[35])
    }

    @Test
    fun buildLocationPacket_timestampAppliesMinus5HoursShift() {
        val cal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 14, 15, 30, 0)
        }
        val timestamp = cal.timeInMillis

        // Build packet: logic goes back -5 hours from given timestamp
        val packet = Gt06Protocol.buildLocationPacket(
            lat = 24.8607,
            lon = 67.0011,
            isIgnitionOn = true,
            serialNo = 1,
            timestampMs = timestamp
        )
        // Hour in BCD at index 7: 15 - 5 = 10 -> BCD 0x10 = 16 or fromBcd
        val hourBcd = packet[7]
        val hour = Gt06Protocol.fromBcd(hourBcd)
        assertEquals(10, hour) // 15:30 minus 5 hours = 10:30
    }

    @Test
    fun buildLocationPacket_ignitionStatusEncodedCorrectly() {
        // Time In: isIgnitionOn = true -> ACC 1
        val packetTimeIn = Gt06Protocol.buildLocationPacket(
            lat = 24.8607,
            lon = 67.0011,
            isIgnitionOn = true,
            serialNo = 1
        )
        val parsedTimeIn = Gt06Protocol.parseLocationPacket(packetTimeIn)
        assertNotNull(parsedTimeIn)
        assertTrue(parsedTimeIn!!.isIgnitionOn)

        // Time Out: isIgnitionOn = false -> ACC 0
        val packetTimeOut = Gt06Protocol.buildLocationPacket(
            lat = 24.8607,
            lon = 67.0011,
            isIgnitionOn = false,
            serialNo = 2
        )
        val parsedTimeOut = Gt06Protocol.parseLocationPacket(packetTimeOut)
        assertNotNull(parsedTimeOut)
        assertFalse(parsedTimeOut!!.isIgnitionOn)
    }

    @Test
    fun buildStatusPacket_timeInAndOutEncodeAccCorrectly() {
        // Time In: isIgnitionOn = true -> ACC 1 (bit 1 is 1)
        val statusTimeIn = Gt06Protocol.buildStatusPacket(isIgnitionOn = true, serialNo = 1)
        assertEquals(15, statusTimeIn.size)
        assertEquals(0x78.toByte(), statusTimeIn[0])
        assertEquals(0x78.toByte(), statusTimeIn[1])
        assertEquals(0x0A.toByte(), statusTimeIn[2]) // length
        assertEquals(0x13.toByte(), statusTimeIn[3]) // protocol 0x13
        val terminalInfoTimeIn = statusTimeIn[4].toInt() and 0xFF
        assertTrue((terminalInfoTimeIn and 0x02) != 0) // Bit 1 = 1 (ACC ON)
        assertEquals(0x47, terminalInfoTimeIn)

        // Time Out: isIgnitionOn = false -> ACC 0 (bit 1 is 0)
        val statusTimeOut = Gt06Protocol.buildStatusPacket(isIgnitionOn = false, serialNo = 2)
        assertEquals(15, statusTimeOut.size)
        val terminalInfoTimeOut = statusTimeOut[4].toInt() and 0xFF
        assertEquals(0, terminalInfoTimeOut and 0x02) // Bit 1 = 0 (ACC OFF)
        assertEquals(0x45, terminalInfoTimeOut)
    }

    @Test
    fun buildVtpImei_patternMatchesSpecification() {
        // 99 (VTP, 2) + 002 (Product, 3) + 1001 (Company, 4) + 0001 (Employee 001, 4) + 01 (Server, 2) = 15 digits
        val imei1 = com.example.util.DeviceInfoManager.buildVtpImei("1001", "001")
        assertEquals(15, imei1.length)
        assertEquals("990021001000101", imei1)

        val imei2 = com.example.util.DeviceInfoManager.buildVtpImei("1001", "452")
        assertEquals(15, imei2.length)
        assertEquals("990021001045201", imei2)

        val imei3 = com.example.util.DeviceInfoManager.buildVtpImei("1001", "1234", "02")
        assertEquals(15, imei3.length)
        assertEquals("990021001123402", imei3)
    }
}

