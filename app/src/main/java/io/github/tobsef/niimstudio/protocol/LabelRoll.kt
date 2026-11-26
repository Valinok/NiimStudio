package io.github.tobsef.niimstudio.protocol

/**
 * RFID data of the loaded label roll (RfidInfo: request 0x1A, response 0x1B).
 *
 * The payload is variable-length: an 8-byte tag uuid followed by two length-prefixed
 * ASCII strings, total/used counters, a type byte and an optional capacity. A payload
 * of one byte means "no readable tag"; the first uuid byte is NOT a presence flag.
 */
data class LabelRoll(
    val uuid: String,
    val barcode: String?,
    val serial: String?,
    val total: Int,
    val used: Int,
    val type: Int,
    val capacity: Int? = null,
) {
    val remaining: Int get() = (total - used).coerceAtLeast(0)

    /**
     * The chip serial is `<paper type, 8 chars><serial, 8 chars>`, e.g.
     * `PZ1G4173` + `35011330`. Short or unexpected values stay unsplit so the raw
     * string can still be shown.
     */
    val paperType: String? get() = serial?.takeIf { it.length > 8 }?.substring(0, 8)
    val serialNumber: String? get() = serial?.takeIf { it.length > 8 }?.substring(8) ?: serial

    /** Safe for logs: the tag UUID and full chip serial are identifiers, not diagnostics. */
    override fun toString() = buildString {
        barcode?.let { append("sku=").append(it) }
        append(" sn=redacted")
        append(" used=").append(used).append('/').append(total)
        append(" left=").append(remaining)
        append(" type=").append(type)
        capacity?.let { append(" capacity=").append(it) }
    }

    companion object {
        private const val UUID_BYTES = 8

        /** Returns null for a missing tag or a truncated/unexpected payload; never throws. */
        fun parse(data: ByteArray): LabelRoll? {
            if (data.size <= 1) return null
            var pos = 0

            fun need(n: Int) = n >= 0 && pos + n <= data.size
            fun u16be(): Int {
                val value = data.u(pos) shl 8 or data.u(pos + 1)
                pos += 2
                return value
            }
            fun readString(): String? {
                if (!need(1)) return null
                val len = data.u(pos)
                pos++
                if (!need(len)) return null
                val value = String(data, pos, len, Charsets.US_ASCII)
                pos += len
                return value
            }

            if (!need(UUID_BYTES)) return null
            val uuid = data.copyOfRange(pos, pos + UUID_BYTES).joinToString("") { "%02X".format(it.toInt() and 0xFF) }
            pos += UUID_BYTES

            val barcode = readString() ?: return null
            val serial = readString() ?: return null
            if (!need(5)) return null // total(2) + used(2) + type(1)
            val total = u16be()
            val used = u16be()
            val type = data.u(pos)
            pos++
            val capacity = if (need(2)) u16be() else null

            return LabelRoll(
                uuid = uuid,
                barcode = barcode.ifEmpty { null },
                serial = serial.ifEmpty { null },
                total = total,
                used = used,
                type = type,
                capacity = capacity,
            )
        }
    }
}
