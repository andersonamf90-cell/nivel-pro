package com.nivelpro.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.nivelpro.app.sensor.FrontalDirection
import com.nivelpro.app.sensor.LateralDirection
import com.nivelpro.app.ui.theme.NivelColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PlumbReticleCanvas(
    lateralDeg: Float,
    frontalDeg: Float,
    isLateralAligned: Boolean,
    isFrontalAligned: Boolean,
    isFullyPlumb: Boolean,
    lateralDirection: LateralDirection,
    frontalDirection: FrontalDirection,
    modifier: Modifier = Modifier
) {
    val animatedLateral by animateFloatAsState(
        targetValue = lateralDeg,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "lateralAngle"
    )

    val animatedFrontal by animateFloatAsState(
        targetValue = frontalDeg,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "frontalAngle"
    )

    val dynamicLineColor by animateColorAsState(
        targetValue = if (isLateralAligned) NivelColors.PlumbGreen else NivelColors.TiltRed,
        animationSpec = tween(durationMillis = 140),
        label = "dynamicLineColor"
    )

    val fixedLineColor by animateColorAsState(
        targetValue = if (isFullyPlumb) NivelColors.PlumbGreen else NivelColors.ReferenceLine.copy(alpha = 0.72f),
        animationSpec = tween(durationMillis = 140),
        label = "fixedLineColor"
    )

    val frontalColor by animateColorAsState(
        targetValue = if (isFrontalAligned) NivelColors.PlumbGreen else NivelColors.TiltRed,
        animationSpec = tween(durationMillis = 140),
        label = "frontalColor"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val center = Offset(cx, cy)
        val radius = min(size.width, size.height) * 0.39f
        val lineHalfLen = min(size.height * 0.44f, radius * 1.18f)

        // 1. Anel externo discreto e marcas de escala angulares (-15° a +15°)
        drawCircle(
            color = NivelColors.ReticleTrack,
            radius = radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawDegreeArcTicks(center = center, radius = radius)

        // 2. Linha vertical FIXA no centro representando o prumo verdadeiro
        val fixedStroke = if (isFullyPlumb) 3.5.dp.toPx() else 2.2.dp.toPx()
        drawLine(
            color = fixedLineColor,
            start = Offset(cx, cy - lineHalfLen),
            end = Offset(cx, cy + lineHalfLen),
            strokeWidth = fixedStroke,
            cap = StrokeCap.Round,
            pathEffect = if (isLateralAligned) null else PathEffect.dashPathEffect(
                floatArrayOf(10.dp.toPx(), 7.dp.toPx()),
                0f
            )
        )

        // Pequenos marcadores horizontais de mira no topo e base do prumo fixo
        val capHalf = 9.dp.toPx()
        drawLine(
            color = fixedLineColor.copy(alpha = 0.75f),
            start = Offset(cx - capHalf, cy - lineHalfLen),
            end = Offset(cx + capHalf, cy - lineHalfLen),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = fixedLineColor.copy(alpha = 0.75f),
            start = Offset(cx - capHalf, cy + lineHalfLen),
            end = Offset(cx + capHalf, cy + lineHalfLen),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 3. Barra de inclinação FRONTAL (frente/trás) ao longo do eixo central
        val maxFrontalVisualDeg = 20f
        val normalizedFrontal = (animatedFrontal / maxFrontalVisualDeg).coerceIn(-1f, 1f)
        val frontalOffsetY = -normalizedFrontal * (radius * 0.65f)
        val crossBarHalf = 22.dp.toPx()

        drawLine(
            color = frontalColor.copy(alpha = 0.9f),
            start = Offset(cx - crossBarHalf, cy + frontalOffsetY),
            end = Offset(cx + crossBarHalf, cy + frontalOffsetY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Ponto central de referência frontal
        drawCircle(
            color = if (isFullyPlumb) NivelColors.PlumbGreen else NivelColors.PureBlack,
            radius = 6.dp.toPx(),
            center = center
        )
        drawCircle(
            color = if (isFullyPlumb) NivelColors.PlumbGreen else NivelColors.ReferenceLine,
            radius = 6.dp.toPx(),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // 4. Linha DINÂMICA que acompanha a inclinação real lateral do celular
        // Quando lateralDeg == 0°, coincide exatamente com a linha fixa central e fica verde.
        rotate(degrees = -animatedLateral, pivot = center) {
            if (isLateralAligned) {
                drawLine(
                    color = dynamicLineColor.copy(alpha = 0.22f),
                    start = Offset(cx, cy - lineHalfLen),
                    end = Offset(cx, cy + lineHalfLen),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            drawLine(
                color = dynamicLineColor,
                start = Offset(cx, cy - lineHalfLen),
                end = Offset(cx, cy + lineHalfLen),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Extremidades com destaque geométrico para leitura rápida
            drawCircle(
                color = dynamicLineColor,
                radius = 4.5.dp.toPx(),
                center = Offset(cx, cy - lineHalfLen)
            )
            drawCircle(
                color = dynamicLineColor,
                radius = 4.5.dp.toPx(),
                center = Offset(cx, cy + lineHalfLen)
            )
        }

        // 5. Setas direcionais de correção lateral (Esquerda / Direita)
        when (lateralDirection) {
            LateralDirection.TILT_RIGHT -> {
                // Topo inclinado para a direita -> seta aponta para a ESQUERDA
                drawDirectionChevron(
                    tip = Offset(cx - radius - 22.dp.toPx(), cy),
                    pointingLeft = true,
                    color = NivelColors.TiltRed
                )
            }
            LateralDirection.TILT_LEFT -> {
                // Topo inclinado para a esquerda -> seta aponta para a DIREITA
                drawDirectionChevron(
                    tip = Offset(cx + radius + 22.dp.toPx(), cy),
                    pointingLeft = false,
                    color = NivelColors.TiltRed
                )
            }
            LateralDirection.ALIGNED -> Unit
        }

        // 6. Setas direcionais de correção frontal (Frente / Trás)
        when (frontalDirection) {
            FrontalDirection.TILT_BACKWARD -> {
                // Topo inclinado para trás (em direção ao usuário) -> seta indica empurrar para frente (topo)
                drawVerticalChevron(
                    tip = Offset(cx, cy - radius - 18.dp.toPx()),
                    pointingUp = true,
                    color = NivelColors.TiltRed
                )
            }
            FrontalDirection.TILT_FORWARD -> {
                // Topo inclinado para frente -> seta indica puxar para trás (base)
                drawVerticalChevron(
                    tip = Offset(cx, cy + radius + 18.dp.toPx()),
                    pointingUp = false,
                    color = NivelColors.TiltRed
                )
            }
            FrontalDirection.ALIGNED -> Unit
        }
    }
}

private fun DrawScope.drawDegreeArcTicks(center: Offset, radius: Float) {
    val angles = listOf(-15, -10, -5, 0, 5, 10, 15)
    for (deg in angles) {
        val tickLen = if (deg == 0) 11.dp.toPx() else 6.dp.toPx()
        val strokeW = if (deg == 0) 2.2.dp.toPx() else 1.2.dp.toPx()
        val color = if (deg == 0) NivelColors.ReferenceLine.copy(alpha = 0.7f) else NivelColors.BorderSubtle

        // Topo (em torno de -90°) e Base (em torno de +90°)
        for (baseAngle in listOf(-90.0, 90.0)) {
            val rad = Math.toRadians(baseAngle + deg)
            val cosA = cos(rad).toFloat()
            val sinA = sin(rad).toFloat()
            val inner = Offset(center.x + (radius - tickLen) * cosA, center.y + (radius - tickLen) * sinA)
            val outer = Offset(center.x + (radius + 2.dp.toPx()) * cosA, center.y + (radius + 2.dp.toPx()) * sinA)
            drawLine(
                color = color,
                start = inner,
                end = outer,
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawDirectionChevron(
    tip: Offset,
    pointingLeft: Boolean,
    color: androidx.compose.ui.graphics.Color
) {
    val dir = if (pointingLeft) 1f else -1f
    val sizePx = 11.dp.toPx()
    val path = Path().apply {
        moveTo(tip.x + dir * sizePx, tip.y - sizePx)
        lineTo(tip.x, tip.y)
        lineTo(tip.x + dir * sizePx, tip.y + sizePx)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawVerticalChevron(
    tip: Offset,
    pointingUp: Boolean,
    color: androidx.compose.ui.graphics.Color
) {
    val dir = if (pointingUp) 1f else -1f
    val sizePx = 9.dp.toPx()
    val path = Path().apply {
        moveTo(tip.x - sizePx, tip.y + dir * sizePx)
        lineTo(tip.x, tip.y)
        lineTo(tip.x + sizePx, tip.y + dir * sizePx)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
}
