package io.github.tobsef.niimstudio.ui

import androidx.compose.ui.graphics.Color
import io.github.tobsef.niimstudio.protocol.CloudName
import io.github.tobsef.niimstudio.protocol.CloudRoll
import io.github.tobsef.niimstudio.protocol.LabelRoll
import org.junit.Assert.assertEquals
import org.junit.Test

class RollColorTest {

    private fun roll(sku: String? = null, serial: String? = null, type: Int = 1) = LabelRoll(
        uuid = "0011223344556677",
        barcode = sku,
        serial = serial,
        total = 100,
        used = 10,
        type = type,
    )

    @Test
    fun knownProductsWinOverMaterialCode() {
        assertEquals(Color(0xFFF2C230), rollColor(roll("6972842743824", "PC0FB21339001361")))
        assertEquals(Color(0xFF4CAF7A), rollColor(roll("6972842743800", "PC0FC11330001306")))
        assertEquals(Color(0xFFF2F2F2), rollColor(roll("01222281", "PZ1G417335011330")))
    }

    @Test
    fun unknownProductFallsBackToMaterialCode() {
        assertEquals(Color(0xFFBFE3F2), rollColor(roll("unknown", "UNKNOWN001", type = 5)))
        assertEquals(Color(0xFFBFC3C9), rollColor(roll("unknown", "UNKNOWN002", type = 8)))
        assertEquals(Color(0xFFF2F2F2), rollColor(roll("unknown", "UNKNOWN003", type = 1)))
    }

    @Test
    fun missingRollIsNeutral() {
        assertEquals(Color(0xFFBDBDC4), rollColor(null))
    }

    @Test
    fun catalogueColourWinsOverTheOfflineGuess() {
        val catalog = CloudRoll(
            name = "T12.5*74+35-60蓝线缆",
            labelNames = listOf(CloudName(languageCode = "en", name = "T12.5*74+35-60BLUE Cable")),
        )
        assertEquals(Color(0xFF3D7BD6), rollColor(null, catalog))
        assertEquals(Color(0xFF3D7BD6), rollColor(roll("01222281", "PZ1G417335011330"), catalog))
    }
}
