package com.nivelpro.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nivelpro.app.data.SmoothingPreset
import com.nivelpro.app.ui.theme.NivelColors
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    toleranceDeg: Float,
    smoothingPreset: SmoothingPreset,
    onToleranceChange: (Float) -> Unit,
    onSmoothingChange: (SmoothingPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val quickPresets = listOf(0.1f, 0.2f, 0.5f, 1.0f, 2.0f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NivelColors.SurfaceDark,
        contentColor = NivelColors.TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tolerância e Filtragem",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NivelColors.TextPrimary
                )
                TextButton(onClick = onDismiss) {
                    Text("Concluir", color = NivelColors.PlumbGreen)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tolerância de Prumo (Verde)",
                        fontSize = 14.sp,
                        color = NivelColors.TextSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "±%.1f°", toleranceDeg),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NivelColors.PlumbGreen
                    )
                }

                Slider(
                    value = toleranceDeg,
                    onValueChange = { onToleranceChange(it) },
                    valueRange = 0.1f..3.0f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = NivelColors.PlumbGreen,
                        activeTrackColor = NivelColors.PlumbGreen,
                        inactiveTrackColor = NivelColors.BorderSubtle
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPresets.forEach { preset ->
                        val selected = abs(toleranceDeg - preset) < 0.05f
                        Surface(
                            onClick = { onToleranceChange(preset) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) NivelColors.PlumbGreenDim else NivelColors.SurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (selected) NivelColors.PlumbGreen else NivelColors.BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f°", preset),
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) NivelColors.PlumbGreen else NivelColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Estabilização da Leitura (Filtro Passa-Baixa)",
                    fontSize = 14.sp,
                    color = NivelColors.TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmoothingPreset.entries.forEach { preset ->
                        val selected = smoothingPreset == preset
                        Surface(
                            onClick = { onSmoothingChange(preset) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) NivelColors.PlumbGreenDim else NivelColors.SurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (selected) NivelColors.PlumbGreen else NivelColors.BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = preset.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) NivelColors.PlumbGreen else NivelColors.TextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
