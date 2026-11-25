package io.github.tobsef.niimstudio.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.tobsef.niimstudio.R
import io.github.tobsef.niimstudio.model.AppSettings
import io.github.tobsef.niimstudio.printer.ConnState
import io.github.tobsef.niimstudio.printer.FailReason
import io.github.tobsef.niimstudio.protocol.LabelRoll
import io.github.tobsef.niimstudio.protocol.LabelType
import io.github.tobsef.niimstudio.protocol.CloudRollColor
import io.github.tobsef.niimstudio.protocol.OriginalLabels
import io.github.tobsef.niimstudio.protocol.PrintProgress
import io.github.tobsef.niimstudio.protocol.PrintStage
import io.github.tobsef.niimstudio.protocol.PrintVariant
import io.github.tobsef.niimstudio.protocol.PrinterException
import io.github.tobsef.niimstudio.protocol.PrinterInfo

/** Localized texts for values coming from the language-neutral printer/protocol layers. */

fun labelTypeRes(id: Int): Int = when (id) {
    LabelType.WITH_GAPS -> R.string.label_type_gaps
    LabelType.CONTINUOUS -> R.string.label_type_continuous
    LabelType.TRANSPARENT -> R.string.label_type_transparent
    LabelType.BLACK_MARK -> R.string.label_type_black_mark
    LabelType.BLACK_MARK_GAP -> R.string.label_type_black_mark_gap
    LabelType.HEAT_SHRINK_TUBE -> R.string.label_type_tube
    else -> R.string.label_type_unknown
}

/** Names of the original roll materials, keyed by the code in OriginalLabels. */
fun originalLabelRes(code: Int): Int = when (code) {
    OriginalLabels.THERMAL_SYNTHETIC -> R.string.mat_thermal_synthetic
    OriginalLabels.PVC_TAG -> R.string.mat_pvc_tag
    OriginalLabels.SYNTHETIC_PAPER -> R.string.mat_synthetic_paper
    OriginalLabels.DT_CARDBOARD -> R.string.mat_dt_cardboard
    OriginalLabels.CLEAR_PET -> R.string.mat_clear_pet
    OriginalLabels.SEMI_GLOSS_PAPER -> R.string.mat_semi_gloss_paper
    OriginalLabels.SEMI_GLOSS_CARDBOARD -> R.string.mat_semi_gloss_cardboard
    OriginalLabels.SILVER_PET -> R.string.mat_silver_pet
    OriginalLabels.WHITE_PET -> R.string.mat_white_pet
    OriginalLabels.WHITE_PVC -> R.string.mat_white_pvc
    OriginalLabels.PREMIUM_DT_PAPER -> R.string.mat_premium_dt_paper
    OriginalLabels.PP_CARDSTOCK -> R.string.mat_pp_cardstock
    OriginalLabels.CLEAR_PE -> R.string.mat_clear_pe
    OriginalLabels.WHITE_PE -> R.string.mat_white_pe
    OriginalLabels.PEARLIZED_FILM -> R.string.mat_pearlized_film
    OriginalLabels.TRANSPARENT_THERMAL -> R.string.mat_transparent_thermal
    OriginalLabels.POLYURETHANE -> R.string.mat_polyurethane
    OriginalLabels.TRANSPARENT_PP_WRAP -> R.string.mat_transparent_pp_wrap
    OriginalLabels.NYLON -> R.string.mat_nylon
    OriginalLabels.THERMAL_RED_PRINT -> R.string.mat_thermal_red_print
    OriginalLabels.THERMAL_RED_BLACK -> R.string.mat_thermal_red_black
    OriginalLabels.THERMAL_LOW_TEMP -> R.string.mat_thermal_low_temp
    OriginalLabels.PET_PAPER -> R.string.mat_pet_paper
    OriginalLabels.HEAT_SHRINK_TUBE -> R.string.mat_heat_shrink_tube
    OriginalLabels.TUBE -> R.string.mat_tube
    else -> R.string.label_type_unknown
}

/**
 * Product name taken from the paper-type prefix of the chip serial (or its SKU).
 * Different products can share the same material `type`, so the material name alone
 * is not enough to tell them apart.
 */
@Composable
fun rollDisplayName(roll: LabelRoll): String {
    val byPaperType = when (roll.paperType) {
        "PC0FB213" -> R.string.roll_yellow_cable
        "PC0FC113" -> R.string.roll_green
        "PZ1G4173" -> R.string.roll_white_t12x40
        else -> null
    }
    val bySku = when (roll.barcode) {
        "6972842743824" -> R.string.roll_yellow_cable
        "6972842743800" -> R.string.roll_green
        "01222281" -> R.string.roll_white_t12x40
        else -> null
    }
    return (byPaperType ?: bySku)?.let { stringResource(it) } ?: stringResource(originalLabelRes(roll.type))
}

/** Translated swatch name for a colour inferred from the cloud catalogue. */
fun cloudColorRes(color: CloudRollColor): Int = when (color) {
    CloudRollColor.YELLOW -> R.string.roll_color_yellow
    CloudRollColor.GREEN -> R.string.roll_color_green
    CloudRollColor.WHITE -> R.string.roll_color_white
    CloudRollColor.BLUE -> R.string.roll_color_blue
    CloudRollColor.RED -> R.string.roll_color_red
    CloudRollColor.BLACK -> R.string.roll_color_black
    CloudRollColor.SILVER -> R.string.roll_color_silver
    CloudRollColor.TRANSPARENT -> R.string.roll_color_transparent
    CloudRollColor.PEARL -> R.string.roll_color_pearl
    CloudRollColor.UNKNOWN -> R.string.roll_color_unknown
}

/** `SKU …` on one line and the full chip SN on the next. */
fun rollIdsText(ctx: Context, roll: LabelRoll): String =
    ctx.getString(R.string.roll_ids, roll.barcode ?: "—", roll.serial ?: "—")

@Composable
fun rollIdsText(roll: LabelRoll): String = rollIdsText(LocalContext.current, roll)

fun variantRes(v: PrintVariant): Int = when (v) {
    PrintVariant.V3 -> R.string.variant_v3
    PrintVariant.LEGACY -> R.string.variant_legacy
    PrintVariant.V4 -> R.string.variant_v4
}

/** Human-readable printer model name; D110_M code comes from the original NIIMBOT config. */
fun printerModelName(info: PrinterInfo): String = when (info.modelId) {
    2320 -> "D110_M"
    3584 -> "B18"
    else -> info.modelId?.let { "ID $it" } ?: "—"
}

/** D110_M is 203 dpi; other models are reported only when the driver knows their resolution. */
fun printerDpi(info: PrinterInfo): Int? = if (info.modelId == 2320) 203 else null

/** Compact one-line summary for connection status and diagnostics. */
fun printerSummary(info: PrinterInfo): String {
    val parts = mutableListOf(printerModelName(info))
    info.battery?.let { parts += "BAT $it/4" }
    info.softwareVersion?.let { parts += "FW $it" }
    return parts.joinToString(" · ")
}

@Composable
fun connText(c: ConnState, s: AppSettings): String = when (c) {
    is ConnState.Connected -> stringResource(R.string.conn_connected, c.name)
    is ConnState.Connecting -> stringResource(R.string.conn_connecting, c.name)
    is ConnState.Failed -> stringResource(R.string.conn_failed, failText(c))
    ConnState.Disconnected -> s.deviceName?.let { stringResource(R.string.conn_disconnected_named, it) }
        ?: stringResource(R.string.conn_no_printer)
}

@Composable
fun failText(c: ConnState.Failed): String = when (c.reason) {
    FailReason.BLUETOOTH_OFF -> stringResource(R.string.fail_bluetooth_off)
    FailReason.NO_PERMISSION -> stringResource(R.string.fail_no_permission)
    FailReason.CONNECT_FAILED -> c.detail ?: stringResource(R.string.fail_connect)
}

@Composable
fun progressText(p: PrintProgress): String = when (p.stage) {
    PrintStage.STARTING -> stringResource(R.string.progress_starting)
    PrintStage.PREPARING -> stringResource(R.string.progress_preparing)
    PrintStage.SENDING -> stringResource(R.string.progress_sending, p.label, p.labels)
    PrintStage.PRINTING -> stringResource(R.string.progress_printing, p.label, p.labels, p.percent)
    PrintStage.DONE -> stringResource(R.string.progress_done)
    PrintStage.CANCELLED -> stringResource(R.string.progress_cancelled)
}

/** Printer error codes (0xDB packet / status byte) with a translation. */
private val printerErrorRes = mapOf(
    0x01 to R.string.perr_cover_open, 0x02 to R.string.perr_no_paper, 0x03 to R.string.perr_low_battery,
    0x04 to R.string.perr_battery, 0x05 to R.string.perr_user_cancel, 0x06 to R.string.perr_data,
    0x07 to R.string.perr_overheat, 0x08 to R.string.perr_paper_out, 0x09 to R.string.perr_busy,
    0x0A to R.string.perr_no_head, 0x0B to R.string.perr_temp_low, 0x0C to R.string.perr_head_loose,
    0x0D to R.string.perr_no_ribbon, 0x0E to R.string.perr_wrong_ribbon, 0x0F to R.string.perr_used_ribbon,
    0x10 to R.string.perr_wrong_paper, 0x16 to R.string.perr_communication, 0x32 to R.string.perr_illegal_page,
    0x14 to R.string.perr_write_rfid_fail, 0x1D to R.string.perr_rfid_tag_not_written,
    0x22 to R.string.perr_rfid_write_unsupported,
    0x34 to R.string.perr_timeout, 0x35 to R.string.perr_non_dedicated_ribbon,
)

/** Message for a failed print/connection, localized where the printer reported a known code. */
fun errorText(ctx: Context, t: Throwable): String {
    if (t is PrinterException && t.code >= 0) {
        val name = printerErrorRes[t.code]?.let(ctx::getString) ?: io.github.tobsef.niimstudio.protocol.PrinterError.name(t.code)
        return ctx.getString(R.string.printer_error, t.code, name)
    }
    return t.message ?: t.javaClass.simpleName
}

private var defaultProfileNames: Map<String, Set<String>>? = null

/**
 * Name to show for a profile: built-in profiles that were not renamed follow the app
 * language, user-defined names are shown unchanged.
 */
fun profileName(p: io.github.tobsef.niimstudio.model.LabelProfile): String {
    val names = defaultProfileNames ?: io.github.tobsef.niimstudio.L10n.available.flatMap { lang ->
        val base = io.github.tobsef.niimstudio.data.Store.appContext
        val c = base.createConfigurationContext(
            android.content.res.Configuration(base.resources.configuration).apply { setLocale(java.util.Locale.forLanguageTag(lang)) },
        )
        io.github.tobsef.niimstudio.model.LabelProfile.defaults(
            c.getString(R.string.profile_white), c.getString(R.string.profile_cable), c.getString(R.string.profile_continuous),
        ) + io.github.tobsef.niimstudio.model.LabelProfile.defaults()
    }.groupBy({ it.id }, { it.name }).mapValues { it.value.toSet() }.also { defaultProfileNames = it }
    if (p.name !in names[p.id].orEmpty()) return p.name
    return io.github.tobsef.niimstudio.data.Store.localizedDefaults().firstOrNull { it.id == p.id }?.name ?: p.name
}
