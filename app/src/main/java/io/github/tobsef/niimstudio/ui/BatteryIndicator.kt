package io.github.tobsef.niimstudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Compact battery in the top bar: an outlined case with four visible bars. */
@Composable
fun BatteryIndicator(level: Int?, modifier: Modifier = Modifier) {
    val fill = when {
        level == null -> AppColors.Divider
        level >= 3 -> AppColors.Ok
        level == 2 -> AppColors.Orange
        else -> AppColors.Accent
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 24.dp, height = 13.dp)
                .border(1.dp, AppColors.TextGrey, RoundedCornerShape(3.dp))
                .padding(2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(4) { index ->
                    Box(
                        Modifier
                            .size(width = 3.dp, height = 7.dp)
                            .background(
                                if (level != null && index < level) fill else AppColors.Divider,
                                RoundedCornerShape(1.dp),
                            ),
                    )
                }
            }
        }
        Box(
            Modifier
                .padding(start = 1.dp)
                .size(width = 2.dp, height = 5.dp)
                .background(AppColors.TextGrey, RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp)),
        )
    }
}
