package io.github.tobsef.niimstudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.stringResource
import io.github.tobsef.niimstudio.R
import io.github.tobsef.niimstudio.protocol.CloudRoll
import io.github.tobsef.niimstudio.protocol.CloudRollColor
import io.github.tobsef.niimstudio.protocol.LabelRoll
import kotlin.math.min

/** Swatch for a colour inferred from the catalogue name (there is no colour field in the chip). */
fun cloudSwatch(color: CloudRollColor): Color = when (color) {
    CloudRollColor.YELLOW -> AppColors.RollYellow
    CloudRollColor.GREEN -> AppColors.RollGreen
    CloudRollColor.WHITE -> AppColors.RollWhite
    CloudRollColor.BLUE -> AppColors.RollBlue
    CloudRollColor.RED -> AppColors.RollRed
    CloudRollColor.BLACK -> AppColors.RollBlack
    CloudRollColor.SILVER -> AppColors.RollSilver
    CloudRollColor.TRANSPARENT -> AppColors.RollTransparent
    CloudRollColor.PEARL -> AppColors.RollPearl
    CloudRollColor.UNKNOWN -> AppColors.Neutral
}

/**
 * Colour of the installed roll. Catalogue colour wins when available; otherwise known
 * products and the material code give an offline fallback. This is our own vector
 * drawing, no vendor artwork is used.
 */
fun rollColor(roll: LabelRoll?, catalog: CloudRoll? = null): Color {
    if (catalog != null && catalog.color != CloudRollColor.UNKNOWN) return cloudSwatch(catalog.color)
    return when {
        roll == null -> AppColors.Neutral
        roll.paperType == "PC0FB213" || roll.barcode == "6972842743824" -> AppColors.RollYellow // yellow cable labels
        roll.paperType == "PC0FC113" || roll.barcode == "6972842743800" -> AppColors.RollGreen // green labels
        roll.paperType == "PZ1G4173" || roll.barcode == "01222281" -> AppColors.RollWhite // white labels
        roll.type == 5 || roll.type == 13 || roll.type == 19 || roll.type == 22 -> AppColors.RollTransparent // clear PET/PE/thermal
        roll.type == 8 -> AppColors.RollSilver // silver PET
        roll.type == 15 -> AppColors.RollPearl // pearlescent film
        else -> AppColors.RollWhite
    }
}

/** Localized name of the swatch shown next to the roll illustration. */
@Composable
fun rollColorName(roll: LabelRoll?, catalog: CloudRoll? = null): String {
    if (catalog != null && catalog.color != CloudRollColor.UNKNOWN) {
        return stringResource(cloudColorRes(catalog.color))
    }
    return when {
        roll == null -> stringResource(R.string.roll_color_unknown)
        roll.paperType == "PC0FB213" || roll.barcode == "6972842743824" -> stringResource(R.string.roll_color_yellow)
        roll.paperType == "PC0FC113" || roll.barcode == "6972842743800" -> stringResource(R.string.roll_color_green)
        roll.paperType == "PZ1G4173" || roll.barcode == "01222281" -> stringResource(R.string.roll_color_white)
        roll.type == 5 || roll.type == 13 || roll.type == 19 || roll.type == 22 -> stringResource(R.string.roll_color_transparent)
        roll.type == 8 -> stringResource(R.string.roll_color_silver)
        roll.type == 15 -> stringResource(R.string.roll_color_pearl)
        else -> stringResource(R.string.roll_color_white)
    }
}

/**
 * The original roll proportions, turned 90° to the left: the tail points up and is
 * 1.5 times longer. The thin arc still shows the remaining part from the chip counters.
 */
@Composable
fun LabelRollImage(roll: LabelRoll?, modifier: Modifier = Modifier, catalog: CloudRoll? = null) {
    val tint = rollColor(roll, catalog)
    val fraction = roll?.let {
        if (it.total > 0) (it.total - it.used).toFloat() / it.total else 0f
    }?.coerceIn(0f, 1f) ?: 0f

    Canvas(modifier) {
        // Geometry of the original drawing, in its own design space.
        val designW = 80f
        val designH = 64f
        val r = min(designW, designH) * 0.32f
        val center = Offset(designW * 0.38f, designH * 0.52f)
        val tailLength = designW * 0.50f * 1.5f // the only changed proportion: 1.5x tail
        val tailLeft = center.x + r * 0.7f
        val tailTop = designH * 0.36f
        val tailHeight = designH * 0.28f
        // Divider is lifted by a quarter of the tail length towards its tip.
        val dividerX = center.x + r * 1.3f + tailLength * 0.25f

        // After the turn, design X becomes screen height and design Y becomes screen width.
        val minX = center.x - r - 4f
        val maxX = tailLeft + tailLength
        val minY = center.y - r - 4f
        val maxY = center.y + r + 4f
        val contentCenter = Offset((minX + maxX) / 2f, (minY + maxY) / 2f)
        val canvasCenter = Offset(size.width / 2f, size.height / 2f)
        val fit = min(size.width / (maxY - minY), size.height / (maxX - minX))

        translate(left = canvasCenter.x - contentCenter.x, top = canvasCenter.y - contentCenter.y) {
            scale(fit, pivot = contentCenter) {
                rotate(-90f, pivot = contentCenter) { // counter-clockwise: the tail points up
                    // Unrolled strip first, so the roll overlaps it naturally.
                    drawRoundRect(
                        color = tint,
                        topLeft = Offset(tailLeft, tailTop),
                        size = Size(tailLength, tailHeight),
                        cornerRadius = CornerRadius(3f, 3f),
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.65f),
                        start = Offset(dividerX, tailTop),
                        end = Offset(dividerX, tailTop + tailHeight),
                        strokeWidth = 1.5f,
                    )

                    drawCircle(color = AppColors.RollHub, radius = r + 3f, center = center)
                    drawCircle(color = tint, radius = r, center = center)
                    drawCircle(color = Color.White, radius = r * 0.34f, center = center)

                    if (roll != null && fraction > 0f) {
                        drawArc(
                            color = AppColors.Ok,
                            startAngle = -90f,
                            sweepAngle = 360f * fraction,
                            useCenter = false,
                            topLeft = Offset(center.x - r - 4f, center.y - r - 4f),
                            size = Size((r + 4f) * 2f, (r + 4f) * 2f),
                            style = Stroke(width = 4f),
                        )
                    }
                }
            }
        }
    }
}
