package io.github.tobsef.niimstudio.protocol

/**
 * Material codes of the original NIIMBOT label rolls.
 *
 * The numeric codes come from the printer catalog bundled with the vendor app
 * (`printerList.json`). Only codes are stored here; localized names live in the
 * UI layer. This is a reference catalog: the code reported by an RFID chip is
 * matched against it, but the codes are NOT used for the 0x23 print command
 * (that command has its own, partly overlapping LabelType enum).
 */
object OriginalLabels {
    const val THERMAL_SYNTHETIC = 1
    const val PVC_TAG = 2
    const val SYNTHETIC_PAPER = 3
    const val DT_CARDBOARD = 4
    const val CLEAR_PET = 5
    const val SEMI_GLOSS_PAPER = 6
    const val SEMI_GLOSS_CARDBOARD = 7
    const val SILVER_PET = 8
    const val WHITE_PET = 9
    const val WHITE_PVC = 10
    const val PREMIUM_DT_PAPER = 11
    const val PP_CARDSTOCK = 12
    const val CLEAR_PE = 13
    const val WHITE_PE = 14
    const val PEARLIZED_FILM = 15
    const val TRANSPARENT_THERMAL = 19
    const val POLYURETHANE = 21
    const val TRANSPARENT_PP_WRAP = 22
    const val NYLON = 23
    const val THERMAL_RED_PRINT = 28
    const val THERMAL_RED_BLACK = 29
    const val THERMAL_LOW_TEMP = 31
    const val PET_PAPER = 35
    const val HEAT_SHRINK_TUBE = 53
    const val TUBE = 54

    /** All materials found in the vendor catalog, in a stable display order. */
    val codes = intArrayOf(
        THERMAL_SYNTHETIC, PVC_TAG, SYNTHETIC_PAPER, DT_CARDBOARD, CLEAR_PET,
        SEMI_GLOSS_PAPER, SEMI_GLOSS_CARDBOARD, SILVER_PET, WHITE_PET, WHITE_PVC,
        PREMIUM_DT_PAPER, PP_CARDSTOCK, CLEAR_PE, WHITE_PE, PEARLIZED_FILM,
        TRANSPARENT_THERMAL, POLYURETHANE, TRANSPARENT_PP_WRAP, NYLON,
        THERMAL_RED_PRINT, THERMAL_RED_BLACK, THERMAL_LOW_TEMP, PET_PAPER,
        HEAT_SHRINK_TUBE, TUBE,
    )

    /** D110_M declares `paperType: "1,5"` in the vendor catalog. */
    val d110mCodes = intArrayOf(THERMAL_SYNTHETIC, CLEAR_PET)

    fun isKnown(code: Int) = codes.contains(code)
    fun isD110m(code: Int) = d110mCodes.contains(code)
}
