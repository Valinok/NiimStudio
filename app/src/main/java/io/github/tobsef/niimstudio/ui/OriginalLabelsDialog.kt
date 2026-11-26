package io.github.tobsef.niimstudio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tobsef.niimstudio.R
import io.github.tobsef.niimstudio.protocol.CloudRoll
import io.github.tobsef.niimstudio.protocol.LabelRoll
import io.github.tobsef.niimstudio.protocol.OriginalLabels

/** Reference list of all original materials; the one detected in the chip is marked. */
@Composable
fun OriginalLabelsDialog(roll: LabelRoll?, catalog: CloudRoll? = null, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.original_labels_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                roll?.let { r ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        LabelRollImage(r, Modifier.size(width = 46.dp, height = 76.dp), catalog)
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(
                                stringResource(
                                    R.string.chip_label,
                                    rollDisplayName(r),
                                    stringResource(R.string.roll_remaining_of, r.remaining, r.total),
                                ),
                                fontSize = 13.sp,
                                color = AppColors.Ok,
                            )
                            Text(rollIdsText(r), fontSize = 11.sp, color = AppColors.TextGrey)
                            Text(
                                stringResource(R.string.roll_material_code, r.type),
                                fontSize = 11.sp, color = AppColors.TextGrey,
                            )
                            Text(
                                stringResource(R.string.roll_color) + ": " + rollColorName(r, catalog),
                                fontSize = 11.sp, color = AppColors.TextGrey,
                            )
                        }
                    }
                }
                for (code in OriginalLabels.codes) {
                    val detected = roll?.type == code
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(originalLabelRes(code)),
                            fontSize = 14.sp,
                            color = if (detected) AppColors.Ok else Color.Unspecified,
                            fontWeight = if (detected) FontWeight.Bold else FontWeight.Normal,
                        )
                        if (detected) {
                            Text(stringResource(R.string.chip_detected), fontSize = 11.sp, color = AppColors.Ok)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}
