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
    val battery: Int?,
    val lidClosed: Boolean?,
    val paperInserted: Boolean?,
    val rfidOk: Boolean?,
) {
    companion object {
        fun parse(data: ByteArray): PrinterStatus? {
            if (data.size < 7) return null

            fun setFlag(index: Int): Boolean? = data.getOrNull(index)?.let { it.toInt() != 0 }

            return PrinterStatus(
                battery = data[2].toInt() and 0xFF,
                // 0 = closed / paper present, any other value = open / missing.
                lidClosed = setFlag(4)?.not(),
                paperInserted = setFlag(5)?.not(),
                rfidOk = setFlag(6),
            )
        }
    }
}
