package com.nivelpro.app.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ExposureZero
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nivelpro.app.sensor.FrontalDirection
import com.nivelpro.app.sensor.LateralDirection
import com.nivelpro.app.ui.components.CalibrationDialog
import com.nivelpro.app.ui.components.PlumbReticleCanvas
import com.nivelpro.app.ui.components.SettingsSheet
import com.nivelpro.app.ui.theme.NivelColors
import java.util.Locale
import kotlin.math.abs

@Composable
fun PlumbScreen(
    state: PlumbUiState,
    onToggleFreeze: () -> Unit,
    onToggleBeep: () -> Unit,
    onOpenCalibration: () -> Unit,
    onCloseCalibration: () -> Unit,
    onCalibrateZero: () -> Unit,
    onResetCalibration: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onToleranceChange: (Float) -> Unit,
    onSmoothingChange: (com.nivelpro.app.data.SmoothingPreset) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NivelColors.PureBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    PlumbReticleCanvas(
                        lateralDeg = state.lateralDeg,
                        frontalDeg = state.frontalDeg,
                        isLateralAligned = state.isLateralAligned,
                        isFrontalAligned = state.isFrontalAligned,
                        isFullyPlumb = state.isFullyPlumb,
                        lateralDirection = state.lateralDirection,
                        frontalDirection = state.frontalDirection
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    TopStatusHeader(state = state, onOpenSettings = onOpenSettings)
                    PrimaryReadoutBlock(state = state)
                    DualAxisCards(state = state)
                    BottomActionDock(
                        state = state,
                        onToggleFreeze = onToggleFreeze,
                        onOpenCalibration = onOpenCalibration,
                        onOpenSettings = onOpenSettings,
                        onToggleBeep = onToggleBeep
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                TopStatusHeader(state = state, onOpenSettings = onOpenSettings)

                PrimaryReadoutBlock(state = state)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PlumbReticleCanvas(
                        lateralDeg = state.lateralDeg,
                        frontalDeg = state.frontalDeg,
                        isLateralAligned = state.isLateralAligned,
                        isFrontalAligned = state.isFrontalAligned,
                        isFullyPlumb = state.isFullyPlumb,
                        lateralDirection = state.lateralDirection,
                        frontalDirection = state.frontalDirection
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DualAxisCards(state = state)
                    BottomActionDock(
                        state = state,
                        onToggleFreeze = onToggleFreeze,
                        onOpenCalibration = onOpenCalibration,
                        onOpenSettings = onOpenSettings,
                        onToggleBeep = onToggleBeep
                    )
                }
            }
        }
    }

    if (state.showCalibrationDialog) {
        CalibrationDialog(
            state = state,
            onCalibrateZero = onCalibrateZero,
            onResetCalibration = onResetCalibration,
            onDismiss = onCloseCalibration
        )
    }

    if (state.showSettingsSheet) {
        SettingsSheet(
            toleranceDeg = state.toleranceDeg,
            smoothingPreset = state.smoothingPreset,
            onToleranceChange = onToleranceChange,
            onSmoothingChange = onSmoothingChange,
            onDismiss = onCloseSettings
        )
    }
}

@Composable
private fun TopStatusHeader(
    state: PlumbUiState,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Nível Pro",
                color = NivelColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = buildString {
                    append(state.sensorSource.label)
                    if (state.hasCustomCalibration) append(" · Zero Calibrado")
                },
                color = NivelColors.TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Surface(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(8.dp),
            color = NivelColors.SurfaceDark,
            border = BorderStroke(1.dp, NivelColors.BorderSubtle)
        ) {
            Text(
                text = String.format(Locale.US, "Tol. ±%.1f°", state.toleranceDeg),
                color = NivelColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun PrimaryReadoutBlock(state: PlumbUiState) {
    val accentColor = when {
        state.isFrozen -> NivelColors.FreezeAmber
        state.isFullyPlumb -> NivelColors.PlumbGreen
        else -> NivelColors.TiltRed
    }

    val statusText = when {
        !state.hasValidSample -> "Aguardando leitura do sensor..."
        state.isFrozen -> "CONGELADO"
        state.isFullyPlumb -> "NO PRUMO"
        else -> buildCorrectionGuidance(state)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format(Locale.US, "%.1f°", abs(state.lateralDeg)),
            style = MaterialTheme.typography.displayLarge,
            color = if (state.hasValidSample) accentColor else NivelColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (state.isFullyPlumb && !state.isFrozen) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "No prumo",
                    tint = NivelColors.PlumbGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = statusText,
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DualAxisCards(state: PlumbUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AxisMetricCard(
            title = "Lateral (Esq / Dir)",
            angleDeg = state.lateralDeg,
            isAligned = state.isLateralAligned,
            directionText = when (state.lateralDirection) {
                LateralDirection.ALIGNED -> "Alinhado"
                LateralDirection.TILT_RIGHT -> "← Mover p/ Esq"
                LateralDirection.TILT_LEFT -> "→ Mover p/ Dir"
            },
            modifier = Modifier.weight(1f)
        )

        AxisMetricCard(
            title = "Frontal (Frente / Trás)",
            angleDeg = state.frontalDeg,
            isAligned = state.isFrontalAligned,
            directionText = when (state.frontalDirection) {
                FrontalDirection.ALIGNED -> "Alinhado"
                FrontalDirection.TILT_BACKWARD -> "↑ Empurrar topo"
                FrontalDirection.TILT_FORWARD -> "↓ Puxar topo"
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AxisMetricCard(
    title: String,
    angleDeg: Float,
    isAligned: Boolean,
    directionText: String,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isAligned) NivelColors.PlumbGreen.copy(alpha = 0.55f) else NivelColors.BorderSubtle
    val valueColor = if (isAligned) NivelColors.PlumbGreen else NivelColors.TiltRed

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NivelColors.SurfaceDark,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = NivelColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format(Locale.US, "%+.1f°", angleDeg),
                    color = valueColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = directionText,
                    color = if (isAligned) NivelColors.PlumbGreen else NivelColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BottomActionDock(
    state: PlumbUiState,
    onToggleFreeze: () -> Unit,
    onOpenCalibration: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleBeep: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DockButton(
            label = if (state.isFrozen) "Retomar" else "Congelar",
            icon = if (state.isFrozen) Icons.Rounded.PlayArrow else Icons.Rounded.AcUnit,
            active = state.isFrozen,
            activeColor = NivelColors.FreezeAmber,
            onClick = onToggleFreeze,
            modifier = Modifier.weight(1.15f)
        )

        DockButton(
            label = "Calibrar",
            icon = Icons.Rounded.ExposureZero,
            active = state.hasCustomCalibration,
            activeColor = NivelColors.PlumbGreen,
            onClick = onOpenCalibration,
            modifier = Modifier.weight(1f)
        )

        DockButton(
            label = "Toler.",
            icon = Icons.Rounded.Tune,
            active = false,
            activeColor = NivelColors.PlumbGreen,
            onClick = onOpenSettings,
            modifier = Modifier.weight(1f)
        )

        DockButton(
            label = if (state.beepEnabled) "Som On" else "Mudo",
            icon = if (state.beepEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
            active = state.beepEnabled,
            activeColor = NivelColors.PlumbGreen,
            onClick = onToggleBeep,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DockButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (active) activeColor.copy(alpha = 0.14f) else NivelColors.SurfaceDark
    val border = if (active) activeColor.copy(alpha = 0.7f) else NivelColors.BorderSubtle
    val tint = if (active) activeColor else NivelColors.TextPrimary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier.height(54.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun buildCorrectionGuidance(state: PlumbUiState): String {
    val parts = mutableListOf<String>()
    when (state.lateralDirection) {
        LateralDirection.TILT_RIGHT -> parts.add("← Inclinar p/ esquerda")
        LateralDirection.TILT_LEFT -> parts.add("→ Inclinar p/ direita")
        LateralDirection.ALIGNED -> Unit
    }
    when (state.frontalDirection) {
        FrontalDirection.TILT_BACKWARD -> parts.add("↑ Empurrar p/ frente")
        FrontalDirection.TILT_FORWARD -> parts.add("↓ Puxar p/ trás")
        FrontalDirection.ALIGNED -> Unit
    }
    return if (parts.isEmpty()) "Ajustando..." else parts.joinToString(" · ")
}
