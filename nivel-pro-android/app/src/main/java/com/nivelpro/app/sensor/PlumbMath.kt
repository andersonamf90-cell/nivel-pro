package com.nivelpro.app.sensor

import android.view.Surface
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt

data class Vector3(
    val x: Float,
    val y: Float,
    val z: Float
)

data class PlumbAngles(
    val lateralDeg: Float,
    val frontalDeg: Float
)

enum class LateralDirection {
    ALIGNED,
    TILT_LEFT,   // Topo inclinado à esquerda -> corrigir para a direita
    TILT_RIGHT   // Topo inclinado à direita -> corrigir para a esquerda
}

enum class FrontalDirection {
    ALIGNED,
    TILT_FORWARD,  // Topo inclinado para frente -> puxar topo para trás
    TILT_BACKWARD  // Topo inclinado para trás -> empurrar topo para frente
}

object PlumbMath {

    /**
     * Remapeia o vetor bruto do sensor (coordenadas físicas do aparelho)
     * para coordenadas alinhadas à orientação atual da tela.
     *
     * Convenção resultante:
     * +X = direita da tela
     * +Y = topo da tela
     * +Z = saindo da tela em direção ao usuário
     */
    fun remapToDisplay(raw: Vector3, surfaceRotation: Int): Vector3 {
        return when (surfaceRotation) {
            Surface.ROTATION_90 -> Vector3(x = -raw.y, y = raw.x, z = raw.z)
            Surface.ROTATION_180 -> Vector3(x = -raw.x, y = -raw.y, z = raw.z)
            Surface.ROTATION_270 -> Vector3(x = raw.y, y = -raw.x, z = raw.z)
            else -> raw
        }
    }

    /**
     * Filtro passa-baixa exponencial aplicado diretamente ao vetor 3D da gravidade
     * para evitar artefatos de descontinuidade angular em ±180°.
     */
    fun lowPassFilter(
        current: Vector3,
        previous: Vector3?,
        alpha: Float
    ): Vector3 {
        if (previous == null) return current
        val clampedAlpha = alpha.coerceIn(0.02f, 1.0f)
        return Vector3(
            x = previous.x + clampedAlpha * (current.x - previous.x),
            y = previous.y + clampedAlpha * (current.y - previous.y),
            z = previous.z + clampedAlpha * (current.z - previous.z)
        )
    }

    /**
     * Calcula inclinação lateral (esquerda/direita) e frontal (frente/trás)
     * em relação ao vetor real da gravidade.
     * Retorna null se a magnitude for inválida (ex.: queda livre).
     */
    fun computeVerticalAngles(gravityDisplay: Vector3): PlumbAngles? {
        val norm = sqrt(
            gravityDisplay.x * gravityDisplay.x +
                gravityDisplay.y * gravityDisplay.y +
                gravityDisplay.z * gravityDisplay.z
        )
        if (norm < 1.0f) return null

        val ux = gravityDisplay.x / norm
        val uy = gravityDisplay.y / norm
        val uz = gravityDisplay.z / norm

        // Ângulo lateral: positivo quando o topo do celular inclina para a direita.
        val lateralRad = atan2(-ux.toDouble(), uy.toDouble())
        val lateralDeg = Math.toDegrees(lateralRad).toFloat()

        // Ângulo frontal: positivo quando o topo inclina para trás (em direção ao usuário).
        val xyMag = hypot(ux.toDouble(), uy.toDouble())
        val frontalRad = atan2(uz.toDouble(), xyMag)
        val frontalDeg = Math.toDegrees(frontalRad).toFloat()

        return PlumbAngles(
            lateralDeg = lateralDeg,
            frontalDeg = frontalDeg
        )
    }

    fun applyCalibration(
        raw: PlumbAngles,
        lateralOffsetDeg: Float,
        frontalOffsetDeg: Float
    ): PlumbAngles {
        return PlumbAngles(
            lateralDeg = wrapAngle180(raw.lateralDeg - lateralOffsetDeg),
            frontalDeg = (raw.frontalDeg - frontalOffsetDeg).coerceIn(-90f, 90f)
        )
    }

    fun wrapAngle180(angleDeg: Float): Float {
        var wrapped = angleDeg % 360f
        if (wrapped > 180f) wrapped -= 360f
        if (wrapped < -180f) wrapped += 360f
        return wrapped
    }

    /**
     * Avalia se um eixo está dentro da tolerância usando histerese suave
     * para eliminar tremulação verde/vermelho no limiar.
     */
    fun isAxisAligned(
        angleDeg: Float,
        toleranceDeg: Float,
        wasAligned: Boolean
    ): Boolean {
        val hysteresis = if (wasAligned) min(0.08f, toleranceDeg * 0.25f) else 0f
        return abs(angleDeg) <= (toleranceDeg + hysteresis)
    }

    fun lateralDirection(lateralDeg: Float, toleranceDeg: Float, isAligned: Boolean): LateralDirection {
        if (isAligned || abs(lateralDeg) <= toleranceDeg) return LateralDirection.ALIGNED
        return if (lateralDeg > 0f) LateralDirection.TILT_RIGHT else LateralDirection.TILT_LEFT
    }

    fun frontalDirection(frontalDeg: Float, toleranceDeg: Float, isAligned: Boolean): FrontalDirection {
        if (isAligned || abs(frontalDeg) <= toleranceDeg) return FrontalDirection.ALIGNED
        return if (frontalDeg > 0f) FrontalDirection.TILT_BACKWARD else FrontalDirection.TILT_FORWARD
    }

    /**
     * Calcula o desvio combinado aproximado em graus para referência rápida.
     */
    fun combinedDeviationDeg(lateralDeg: Float, frontalDeg: Float): Float {
        val total = hypot(lateralDeg.toDouble(), frontalDeg.toDouble()).toFloat()
        return min(total, (180.0 / PI).toFloat() * PI.toFloat())
    }
}
