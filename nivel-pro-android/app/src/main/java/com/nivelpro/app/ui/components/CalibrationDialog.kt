package com.nivelpro.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nivelpro.app.ui.PlumbUiState
import com.nivelpro.app.ui.theme.NivelColors
import java.util.Locale

@Composable
fun CalibrationDialog(
    state: PlumbUiState,
    onCalibrateZero: () -> Unit,
    onResetCalibration: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NivelColors.SurfaceDark,
            border = BorderStroke(1.dp, NivelColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Calibração e Conferência",
                    color = NivelColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Comparação leitura bruta vs calibrada
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NivelColors.SurfaceElevated,
                    border = BorderStroke(1.dp, NivelColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Leitura dos Sensores (${state.sensorSource.label})",
                            color = NivelColors.TextSecondary,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bruto (Fábrica):",
                                color = NivelColors.TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Lat %.1f° | Front %.1f°",
                                    state.rawLateralDeg,
                                    state.rawFrontalDeg
                                ),
                                color = NivelColors.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Calibrado (Atual):",
                                color = NivelColors.TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Lat %.1f° | Front %.1f°",
                                    state.lateralDeg,
                                    state.frontalDeg
                                ),
                                color = NivelColors.PlumbGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (state.hasCustomCalibration) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Deslocamento Salvo:",
                                    color = NivelColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "ΔLat %+.1f° | ΔFront %+.1f°",
                                        state.lateralOffsetDeg,
                                        state.frontalOffsetDeg
                                    ),
                                    color = NivelColors.FreezeAmber,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                if (state.isCalibrating) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Coletando média de 25 amostras reais... Mantenha o aparelho imóvel.",
                            color = NivelColors.PlumbGreen,
                            fontSize = 13.sp
                        )
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = NivelColors.PlumbGreen,
                            trackColor = NivelColors.BorderSubtle
                        )
                    }
                }

                // Nota técnica honesta sobre resolução vs precisão e conferência com nível físico
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NivelColors.PureBlack,
                    border = BorderStroke(1.dp, NivelColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Como conferir com um nível físico de referência",
                            color = NivelColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "1. Encoste a lateral do Poco X7 Pro junto a um nível de bolha ou prumo físico confiável (cuidado com a saliência das câmeras ou capinha).\n" +
                                "2. Com o nível físico exatamente no prumo, toque em \"Definir Zero Atual\".\n" +
                                "3. Teste de reversão: gire o aparelho 180° (face oposta ou invertido) na mesma superfície; se houver diferença simétrica, o erro mecânico foi compensado.\n" +
                                "Nota: A resolução exibida é de 0,1°, mas a precisão real depende do sensor MEMS, temperatura e apoio mecânico.",
                            color = NivelColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Button(
                    onClick = onCalibrateZero,
                    enabled = !state.isCalibrating && state.hasValidSample,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NivelColors.PlumbGreen,
                        contentColor = NivelColors.PureBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (state.isCalibrating) "Calibrando..." else "Definir Zero Atual",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onResetCalibration,
                    enabled = state.hasCustomCalibration && !state.isCalibrating,
                    border = BorderStroke(1.dp, NivelColors.BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NivelColors.TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("Restaurar Padrão de Fábrica")
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = NivelColors.TextSecondary)
                    }
                }
            }
        }
    }
}
