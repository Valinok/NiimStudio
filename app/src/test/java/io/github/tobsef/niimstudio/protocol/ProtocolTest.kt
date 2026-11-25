package io.github.tobsef.niimstudio.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolTest {

    private fun hex(s: String) = s.split(" ").filter { it.isNotEmpty() }.map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun autoVariantUsesLegacyForD110M() {
        assertEquals(PrintVariant.LEGACY, autoPrintVariant(PrinterInfo(modelId = 2320, protocolVersion = 5)))
        assertEquals(PrintVariant.V4, autoPrintVariant(PrinterInfo(protocolVersion = 4)))
        assertEquals(PrintVariant.V3, autoPrintVariant(null))
    }

    @Test
    fun rfidInfoPacketEncoding() {
        assertArrayEquals(hex("55 55 1A 01 01 1A AA AA"), NiimbotPacket(Cmd.RFID_INFO).toBytes())
    }

    @Test
    fun rfidParserReadsRollWithCapacity() {
        val d = hex("00 11 22 33 44 55 66 77") +
            byteArrayOf(6) + "A1B2C3".toByteArray(Charsets.US_ASCII) +
            byteArrayOf(4) + "1234".toByteArray(Charsets.US_ASCII) +
            hex("00 E6 00 0C 01 00 C8")
        val roll = LabelRoll.parse(d)!!
        assertEquals("0011223344556677", roll.uuid)
        assertEquals("A1B2C3", roll.barcode)
        assertEquals("1234", roll.serial)
        assertEquals(230, roll.total)
        assertEquals(12, roll.used)
        assertEquals(218, roll.remaining)
        assertEquals(1, roll.type)
        assertEquals(200, roll.capacity)
    }

    @Test
    fun rfidParserToleratesZeroUuidAndNoCapacity() {
        val d = byteArrayOf(0) + ByteArray(7) + byteArrayOf(0, 0) + hex("00 32 00 32 03")
        val roll = LabelRoll.parse(d)!!
        assertEquals("0000000000000000", roll.uuid)
        assertNull(roll.barcode)
        assertNull(roll.serial)
        assertNull(roll.capacity)
        assertEquals(50, roll.total)
        assertEquals(50, roll.used)
        assertEquals(0, roll.remaining)
        assertEquals(3, roll.type)
    }

    @Test
    fun rfidChipSerialSplitsPaperTypeAndSerialNumber() {
        val d = hex("00 11 22 33 44 55 66 77") +
            byteArrayOf(8) + "01222281".toByteArray(Charsets.US_ASCII) +
            byteArrayOf(16) + "PZ1G417335011330".toByteArray(Charsets.US_ASCII) +
            hex("00 C0 00 47 01 01 10")
        val roll = LabelRoll.parse(d)!!
        assertEquals("01222281", roll.barcode)
        assertEquals("PZ1G417335011330", roll.serial)
        assertEquals("PZ1G4173", roll.paperType)
        assertEquals("35011330", roll.serialNumber)
        assertEquals(121, roll.remaining)
    }

    @Test
    fun rfidShortSerialIsNotSplit() {
        val d = hex("00 11 22 33 44 55 66 77") +
            byteArrayOf(0, 4) + "1234".toByteArray(Charsets.US_ASCII) +
            hex("00 32 00 32 03")
        val roll = LabelRoll.parse(d)!!
        assertEquals("1234", roll.serial)
        assertNull(roll.paperType)
        assertEquals("1234", roll.serialNumber)
    }

    @Test
    fun rfidParserRejectsAbsentAndTruncatedPayloads() {
        assertNull(LabelRoll.parse(byteArrayOf(1)))
        assertNull(LabelRoll.parse(byteArrayOf(0, 1, 2)))
        val truncated = hex("00 11 22 33 44 55 66 77") + byteArrayOf(5) + hex("41 42 43 44")
        assertNull(LabelRoll.parse(truncated))
        val shortTail = hex("00 11 22 33 44 55 66 77 00 00 00 01 02")
        assertNull(LabelRoll.parse(shortTail))
    }

    @Test
    fun rfidParserClampsNegativeRemaining() {
        val d = hex("00 11 22 33 44 55 66 77 00 00 00 0A 00 14 01")
        assertEquals(0, LabelRoll.parse(d)!!.remaining)
    }

    @Test
    fun heartbeatPacketEncoding() {
        // 55 55 DC 01 01 XOR(DC,01,01)=DC AA AA
        assertArrayEquals(hex("55 55 DC 01 01 DC AA AA"), NiimbotPacket(Cmd.HEARTBEAT, byteArrayOf(1)).toBytes())
    }

    @Test
    fun connectPacketHasPrefix() {
        assertArrayEquals(hex("03 55 55 C1 01 01 C1 AA AA"), NiimbotPacket(Cmd.CONNECT).toBytes())
    }

    @Test
    fun parserHandlesSplitAndMergedFrames() {
        val p = PacketStreamParser()
        val a = NiimbotPacket(Rsp.SET_DENSITY, byteArrayOf(1)).toBytes()
        val b = NiimbotPacket(Rsp.PRINT_STATUS, byteArrayOf(0, 1, 100, 100)).toBytes()
        val stream = byteArrayOf(0x00, 0x13) + a + b
        assertEquals(0, p.feed(stream.copyOfRange(0, 5)).size)
        val got = p.feed(stream.copyOfRange(5, stream.size))
        assertEquals(listOf(Rsp.SET_DENSITY, Rsp.PRINT_STATUS), got.map { it.command })
        assertArrayEquals(byteArrayOf(0, 1, 100, 100), got[1].data)
    }

    @Test
    fun parserSkipsCorruptFrame() {
        val p = PacketStreamParser()
        val bad = NiimbotPacket(0x31, byteArrayOf(1)).toBytes().also { it[5] = 0x00 }
        val good = NiimbotPacket(0x33, byteArrayOf(1)).toBytes()
        assertEquals(listOf(0x33), p.feed(bad + good).map { it.command })
    }

    @Test
    fun imageEncodingAndRowPackets() {
        // 96 x 4 image: row 0 empty, rows 1-2 identical (first 10 dots), row 3 has 1 dot at x=95.
        val cols = 96
        val black = BooleanArray(cols * 4)
        for (y in 1..2) for (x in 0 until 10) black[y * cols + x] = true
        black[3 * cols + 95] = true
        val enc = ImageEncoder.encode(MonoImage(cols, 4, black))
        assertEquals(3, enc.rowsData.size)
        assertNull(enc.rowsData[0].bits)
        assertEquals(2, enc.rowsData[1].repeat)

        val pk = ImageEncoder.rowPackets(enc, 96)
        assertEquals(Cmd.EMPTY_ROW, pk[0].command)
        assertArrayEquals(byteArrayOf(0, 0, 1), pk[0].data)

        // 10 dots -> full bitmap row: row u16, counts per 32-dot chunk (10,0,0), repeat 2, 12 bytes data
        assertEquals(Cmd.BITMAP_ROW, pk[1].command)
        val d = pk[1].data
        assertEquals(2 + 3 + 1 + 12, d.size)
        assertArrayEquals(byteArrayOf(0, 1, 10, 0, 0, 2), d.copyOfRange(0, 6))
        assertEquals(0xFF.toByte(), d[6])
        assertEquals(0xC0.toByte(), d[7])

        // single dot -> indexed packet with index 95
        assertEquals(Cmd.BITMAP_ROW_INDEXED, pk[2].command)
        assertArrayEquals(byteArrayOf(0, 3, 0, 0, 1, 1, 0, 95), pk[2].data)
    }

    @Test
    fun pixelCountsTotalModeForWideRows() {
        val bits = ByteArray(48) { 0xFF.toByte() } // 384 dots, head 96 -> doesn't fit split mode
        assertArrayEquals(byteArrayOf(0, (384 and 0xFF).toByte(), 1), ImageEncoder.pixelCounts(bits, 96))
    }

    @Test
    fun repeatIsCappedAt255() {
        val enc = ImageEncoder.encode(MonoImage(8, 600, BooleanArray(8 * 600)))
        assertEquals(listOf(255, 255, 90), enc.rowsData.map { it.repeat })
    }

    @Test
    fun heartbeatStatusParsesRealD110MPayload() {
        val s = PrinterStatus.parse(byteArrayOf(0x0F, 0xE2.toByte(), 0x04, 0x4A, 0, 0, 1, 0, 0, 0, 0))
        assertNotNull(s)
        assertEquals(4, s!!.battery)
        assertEquals(true, s.lidClosed)
        assertEquals(true, s.paperInserted)
        assertEquals(true, s.rfidOk)
    }

    @Test
    fun heartbeatStatusDetectsOpenLidAndMissingPaper() {
        val s = PrinterStatus.parse(byteArrayOf(0x0F, 0xE2.toByte(), 0x04, 0x4A, 1, 1, 0, 0, 0, 0, 0))
        assertNotNull(s)
        assertEquals(false, s!!.lidClosed)
        assertEquals(false, s.paperInserted)
        assertEquals(false, s.rfidOk)
    }

    @Test
    fun heartbeatStatusRejectsShortPayload() {
        assertNull(PrinterStatus.parse(byteArrayOf(0x0F, 0xE2.toByte(), 0x04, 0x4A, 0, 0)))
    }

    @Test
    fun originalLabelsCatalogHasD110mMaterials() {
        assertEquals(25, OriginalLabels.codes.size)
        assertTrue(OriginalLabels.isD110m(OriginalLabels.THERMAL_SYNTHETIC))
        assertTrue(OriginalLabels.isD110m(OriginalLabels.CLEAR_PET))
        assertFalse(OriginalLabels.isD110m(OriginalLabels.PVC_TAG))
        assertTrue(OriginalLabels.isKnown(OriginalLabels.HEAT_SHRINK_TUBE))
        assertFalse(OriginalLabels.isKnown(99))
    }
}
