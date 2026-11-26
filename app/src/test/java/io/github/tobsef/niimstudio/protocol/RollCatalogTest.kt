package io.github.tobsef.niimstudio.protocol

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class RollCatalogTest {

    private fun response(name: String, cable: Boolean = false) = """
        {"code":1,"message":"ok","data":{
          "name":"$name",
          "names":[{"languageCode":"zh-cn","name":"$name"}],
          "labelNames":[{"languageCode":"zh-cn","name":"$name"},{"languageCode":"en","name":"T12.5*74+35-60 GREEN Cable"}],
          "barcode":"010424815",
          "width":74,"height":12.5,
          "isCable":$cable,"cableLength":${if (cable) 35 else "null"},
          "sceneTags":${if (cable) "[\"cable\"]" else "[]"},
          "paperType":1,"consumableType":30,"consumableTypeTextId":"app100000909",
          "previewImage":"https://example.invalid/label.png","elements":[{"width":74,"height":4.7}]
        }}
    """.trimIndent()

    @Test
    fun parsesOnlyTheFieldsWeNeed() {
        val roll = RollCatalog.parse(response("T12.5*74+35-60绿线缆", cable = true))
        assertEquals("T12.5*74+35-60绿线缆", roll.name)
        assertEquals("T12.5*74+35-60 GREEN Cable", roll.nameFor("en"))
        assertEquals(74.0, roll.width)
        assertEquals(12.5, roll.height)
        assertEquals(30, roll.consumableType)
        assertEquals("app100000909", roll.consumableTypeTextId)
    }

    @Test
    fun cableIsTakenFromCatalogueFlags() {
        assertTrue(RollCatalog.parse(response("T12.5*74+35-60绿线缆", cable = true)).cable)
        assertFalse(RollCatalog.parse(response("T12*40-160白色B款")).cable)
    }

    @Test
    fun colourComesFromTheName() {
        assertEquals(CloudRollColor.GREEN, RollCatalog.parse(response("T12.5*74+35-60绿线缆")).color)
        assertEquals(CloudRollColor.YELLOW, CloudRollColor.from(listOf("RXL12.5*109-65 黄")))
        assertEquals(CloudRollColor.WHITE, CloudRollColor.from(listOf("T12*40-155WHITE")))
        assertEquals(CloudRollColor.UNKNOWN, CloudRollColor.from(listOf("T12*40-160 B")))
    }

    @Test
    fun requestContainsOnlyTheSku() {
        val body = RollCatalog.requestBody("6972842743794")
        val obj = Json.parseToJsonElement(body) as JsonObject
        assertEquals(setOf("oneCode"), obj.keys)
        assertEquals("6972842743794", (obj["oneCode"] as JsonPrimitive).content)
        assertFalse(body.contains("serial", ignoreCase = true))
        assertFalse(body.contains("uuid", ignoreCase = true))
        assertFalse(body.contains("mac", ignoreCase = true))
    }

    @Test(expected = java.io.IOException::class)
    fun errorEnvelopeIsRejected() {
        RollCatalog.parse("""{"code":0,"message":"parameter_error","data":null}""")
    }

    @Test
    fun namesFallBackWhenLabelNamesAreMissing() {
        val roll = RollCatalog.parse("""{"code":1,"data":{"name":"x","names":[{"languageCode":"ru","name":"Синий"}]}}""")
        assertEquals("Синий", roll.nameFor("ru"))
        assertEquals(CloudRollColor.BLUE, roll.color)
    }

    @Test
    fun compoundColourNamesUseTheLastMentionedColour() {
        assertEquals(CloudRollColor.BLACK, CloudRollColor.from(listOf("красный-чёрный")))
        assertEquals(CloudRollColor.BLUE, CloudRollColor.from(listOf("透明蓝线缆")))
        assertEquals(CloudRollColor.RED, CloudRollColor.from(listOf("T12*40 white-red")))
    }

    @Test
    fun languageCodesUseRootLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale("tr", "TR"))
            val roll = CloudRoll(labelNames = listOf(CloudName(languageCode = "id", name = "Etiket")))
            assertEquals("Etiket", roll.nameFor("ID"))
        } finally {
            Locale.setDefault(original)
        }
    }
}
