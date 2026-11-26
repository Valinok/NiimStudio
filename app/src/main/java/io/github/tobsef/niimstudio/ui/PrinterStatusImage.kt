package io.github.tobsef.niimstudio.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import io.github.tobsef.niimstudio.printer.ConnState
import io.github.tobsef.niimstudio.protocol.PrintProgress
import io.github.tobsef.niimstudio.protocol.PrinterStatus

/** Visual state of the printer LED, derived from data we actually receive. */
enum class LedState { OFF, CONNECTING, READY, PRINTING, ERROR }

fun ledState(conn: ConnState, printing: PrintProgress?, status: PrinterStatus?): LedState = when {
    conn is ConnState.Failed -> LedState.ERROR
    conn is ConnState.Connecting -> LedState.CONNECTING
    conn !is ConnState.Connected -> LedState.OFF
    printing != null -> LedState.PRINTING
    status?.lidClosed == false || status?.paperInserted == false || status?.rfidOk == false -> LedState.ERROR
    else -> LedState.READY
}

/**
 * Our own simplified printer illustration: the lid lifts when the cover is open
 * and the LED dot reflects [led]. No vendor artwork is used.
 */
@Composable
fun PrinterStatusImage(status: PrinterStatus?, led: LedState, modifier: Modifier = Modifier) {
    val blink = if (led == LedState.CONNECTING || led == LedState.PRINTING) {
        val transition = rememberInfiniteTransition(label = "printer-led")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
            label = "printer-led-alpha",
        )
    } else null

    val color = when (led) {
        LedState.OFF -> AppColors.Neutral
        LedState.CONNECTING -> AppColors.LedConnecting
        LedState.READY -> AppColors.Ok
        LedState.PRINTING -> AppColors.LedPrinting
        LedState.ERROR -> AppColors.Accent
    }
    val alpha = blink?.value ?: 1f
    val lidOpen = status?.lidClosed == false

    Canvas(modifier = modifier) {
        val w = size.width
        // A full-width lid rotated by 30° needs more headroom than a narrow one.
        val topReserve = 36.dp.toPx()
        val bodyHeight = size.height - topReserve
        val bodyTop = bodyHeight * 0.26f
        val radius = CornerRadius(8f, 8f)

        // The open lid hinges on the left edge and rises above the body. The reserve
        // stays inside the canvas, so the drawing cannot spill onto the buttons below.
        translate(0f, topReserve) {
            // Body.
            drawRoundRect(
                color = AppColors.PrinterBody,
                topLeft = Offset(0f, bodyTop),
                size = Size(w, bodyHeight - bodyTop),
                cornerRadius = CornerRadius(10f, 10f),
            )
            // Lid: same width as the body and flush with its top edge when closed.
            // Opening lifts the right side by 30° around the lower-left hinge.
            val lidLeft = 0f
            val lidWidth = w
            val lidHeight = bodyHeight * 0.24f
            val lidTop = bodyTop - lidHeight
            rotate(
                degrees = if (lidOpen) -30f else 0f,
                pivot = Offset(lidLeft, lidTop + lidHeight), // lower point of the left edge
            ) {
                drawRoundRect(
                    color = AppColors.PrinterLid,
                    topLeft = Offset(lidLeft, lidTop),
                    size = Size(lidWidth, lidHeight),
                    cornerRadius = radius,
                )
                // Dark latch across the lid near its right edge, rotating with the lid.
                drawRoundRect(
                    color = AppColors.LidLatch,
                    topLeft = Offset(lidLeft + lidWidth * 0.88f, lidTop + lidHeight * 0.12f),
                    size = Size(w * 0.05f, lidHeight * 0.76f),
                    cornerRadius = CornerRadius(2f, 2f),
                )
            }
            // Four full-height facets, twice as wide and spread evenly over the body width.
            val barInset = bodyHeight * 0.02f
            val barTop = bodyTop + barInset
            val barHeight = bodyHeight - barInset - barTop
            val barWidth = w * 0.14f
            val gap = (w - 4 * barWidth) / 5f
            repeat(4) { i ->
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(gap + i * (barWidth + gap), barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2f, 2f),
                )
            }
            // LED on the top layer, in the geometric centre of the body.
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = minOf(w, bodyHeight) * 0.10f,
                center = Offset(w / 2f, (bodyTop + bodyHeight) / 2f),
            )
        }
    }
}
