package com.example

import com.example.protocol.Gt06Protocol
import org.junit.Assert.assertEquals
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
    fun buildVtpImei_patternMatchesSpecification() {
        // 99 (VTP) + 002 (Product) + 1001 (Company) + 000001 (Employee 001 padded to 6)
        val imei1 = com.example.util.DeviceInfoManager.buildVtpImei("1001", "001")
        assertEquals(15, imei1.length)
        assertEquals("990021001000001", imei1)

        val imei2 = com.example.util.DeviceInfoManager.buildVtpImei("1001", "452")
        assertEquals(15, imei2.length)
        assertEquals("990021001000452", imei2)

        val imei3 = com.example.util.DeviceInfoManager.buildVtpImei("1001", "123456")
        assertEquals(15, imei3.length)
        assertEquals("990021001123456", imei3)
    }
}

