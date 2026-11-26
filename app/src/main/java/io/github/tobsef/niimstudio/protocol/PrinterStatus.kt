package io.github.tobsef.niimstudio.protocol

/**
 * Status flags from heartbeat type 4 (request 0xDC 04, response 0xD9).
 *
 * Verified on D110_M traffic: data[2] is the battery level (1..4),
 * data[4]=0 means "lid closed", data[5]=0 means "paper inserted",
 * data[6]!=0 means "RFID read ok". A short or unknown payload is not an error:
 * the caller simply keeps the last known status.
 */
data class PrinterStatus(
    val battery: Int,
    val lidClosed: Boolean,
    val paperInserted: Boolean,
    val rfidOk: Boolean,
) {
    companion object {
        /** A payload shorter than 7 bytes is not a status frame; all fields are guaranteed here. */
        fun parse(data: ByteArray): PrinterStatus? {
            if (data.size < 7) return null
            return PrinterStatus(
                battery = data[2].toInt() and 0xFF,
                // 0 = closed / paper present, any other value = open / missing.
                lidClosed = data[4].toInt() == 0,
                paperInserted = data[5].toInt() == 0,
                rfidOk = data[6].toInt() != 0,
            )
        }
    }
}
