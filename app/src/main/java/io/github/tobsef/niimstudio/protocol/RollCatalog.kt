package io.github.tobsef.niimstudio.protocol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Minimal read-only model of the public NIIMBOT cloud template response.
 *
 * The RFID chip gives us a SKU, counters and a material code, but no colour, size or
 * friendly name. The vendor app resolves those by asking the cloud catalogue for the
 * SKU; we do the same here, but keep only the fields we actually display. No serial
 * number, UUID or device identifier is sent.
 */
@Serializable
data class CloudRoll(
    val name: String? = null,
    val names: List<CloudName> = emptyList(),
    @SerialName("labelNames") val labelNames: List<CloudName> = emptyList(),
    val barcode: String? = null,
    @SerialName("previewImage") val previewImage: String? = null,
    val width: Double? = null,
    val height: Double? = null,
    @SerialName("isCable") val isCable: Boolean = false,
    @SerialName("cableLength") val cableLength: Int? = null,
    @SerialName("cableDirection") val cableDirection: Int? = null,
    @SerialName("sceneTags") val sceneTags: List<String> = emptyList(),
    @SerialName("paperType") val paperType: Int? = null,
    @SerialName("consumableType") val consumableType: Int? = null,
    @SerialName("consumableTypeTextId") val consumableTypeTextId: String? = null,
) {
    /** `labelNames` is the localized list; older responses only have `names`. */
    val localizedNames: List<CloudName> get() = labelNames.ifEmpty { names }

    /** Cable labels are flagged explicitly, by scene tag or by a cable length. */
    val cable: Boolean
        get() = isCable || cableLength != null || sceneTags.any { it.equals("cable", ignoreCase = true) }

    /** Catalogue colour is inferred from the name: there is no dedicated colour field. */
    val color: CloudRollColor
        get() = CloudRollColor.from(listOfNotNull(name) + localizedNames.mapNotNull { it.name })

    fun nameFor(language: String): String? {
        val lang = language.lowercase()
        return localizedNames.firstOrNull { it.languageCode?.lowercase() == lang }?.name
            ?: localizedNames.firstOrNull { it.languageCode?.lowercase()?.startsWith(lang) == true }?.name
            ?: name
    }
}

@Serializable
data class CloudName(
    @SerialName("languageCode") val languageCode: String? = null,
    val name: String? = null,
)

/** Language-neutral colour key; the UI maps it to a swatch and a translated label. */
enum class CloudRollColor {
    YELLOW, GREEN, WHITE, BLUE, RED, BLACK, SILVER, TRANSPARENT, PEARL, UNKNOWN;

    companion object {
        fun from(names: List<String>): CloudRollColor {
            val text = names.joinToString(" ").lowercase(Locale.ROOT)
            return when {
                "黄" in text || "黃" in text || "yellow" in text || "huang" in text ||
                    "gelb" in text || "жёлтый" in text || "желтый" in text -> YELLOW
                "绿" in text || "綠" in text || "green" in text || "grün" in text ||
                    "gruen" in text || "зелёный" in text || "зеленый" in text -> GREEN
                "白" in text || "white" in text || "bai" in text ||
                    "weiß" in text || "weiss" in text || "белый" in text -> WHITE
                "蓝" in text || "藍" in text || "blue" in text || "blau" in text ||
                    "синий" in text -> BLUE
                "红" in text || "紅" in text || "red" in text || "rot" in text ||
                    "красный" in text -> RED
                "黑" in text || "black" in text || "hei" in text || "schwarz" in text ||
                    "чёрный" in text || "черный" in text -> BLACK
                "银" in text || "銀" in text || "silver" in text || "silber" in text ||
                    "серебр" in text -> SILVER
                "透明" in text || "transparent" in text || "clear" in text ||
                    "прозрач" in text -> TRANSPARENT
                "珠光" in text || "pearl" in text || "perlglanz" in text ||
                    "perlmutt" in text || "перламутр" in text -> PEARL
                else -> UNKNOWN
            }
        }
    }
}

@Serializable
private data class CloudEnvelope(
    val code: Int = 0,
    val message: String? = null,
    val data: CloudRoll? = null,
)

/** Public catalogue lookup by SKU. Only `oneCode` is sent to the server. */
object RollCatalog {
    private const val ENDPOINT = "https://print.niimbot.com/api/template/getCloudTemplateByOneCode"
    private const val USER_AGENT = "AppVersionName/6.6.5"
    private const val TIMEOUT_MS = 10_000
    private const val MAX_RESPONSE_CHARS = 256 * 1024

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /** Parses a catalogue response; kept public so it can be unit-tested offline. */
    fun parse(body: String): CloudRoll {
        val envelope = json.decodeFromString<CloudEnvelope>(body)
        if (envelope.code != 1 || envelope.data == null) {
            throw IOException(envelope.message ?: "catalogue error ${envelope.code}")
        }
        return envelope.data
    }

    /** Exposed for offline tests: the request must contain the SKU and nothing else. */
    internal fun requestBody(sku: String): String =
        buildJsonObject { put("oneCode", JsonPrimitive(sku)) }.toString()

    private fun readBounded(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        val sb = StringBuilder()
        stream.bufferedReader(Charsets.UTF_8).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                if (sb.length + line.length + 1 > MAX_RESPONSE_CHARS) {
                    throw IOException("Catalogue response too large")
                }
                sb.append(line).append('\n')
                line = reader.readLine()
            }
        }
        return sb.toString()
    }

    suspend fun fetch(sku: String): CloudRoll = withContext(Dispatchers.IO) {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            // A redirect could downgrade HTTPS and leak the SKU to another host.
            instanceFollowRedirects = false
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("niimbot-user-agent", USER_AGENT)
        }
        try {
            val request = requestBody(sku)
            connection.outputStream.use { it.write(request.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val body = readBounded(if (status in 200..299) connection.inputStream else connection.errorStream)
            if (status !in 200..299) throw IOException("HTTP $status: ${body.take(200)}")
            parse(body)
        } finally {
            connection.disconnect()
        }
    }
}
