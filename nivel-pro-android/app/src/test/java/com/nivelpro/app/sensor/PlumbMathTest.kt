package com.nivelpro.app.sensor

import android.view.Surface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class PlumbMathTest {

    @Test
    fun `upright phone in portrait reads 0 degrees lateral and frontal`() {
        val g = Vector3(0f, 9.81f, 0f)
        val angles = PlumbMath.computeVerticalAngles(g)
        assertNotNull(angles)
        assertEquals(0f, angles!!.lateralDeg, 0.01f)
        assertEquals(0f, angles.frontalDeg, 0.01f)
    }

    @Test
    fun `phone tilted 10 degrees right reads +10 degrees lateral`() {
        val rad = Math.toRadians(10.0)
        // Topo inclinado para a direita: componente X do vetor para cima fica negativo
        val g = Vector3(
            x = (-9.81 * sin(rad)).toFloat(),
            y = (9.81 * cos(rad)).toFloat(),
            z = 0f
        )
        val angles = PlumbMath.computeVerticalAngles(g)!!
        assertEquals(10f, angles.lateralDeg, 0.05f)
        assertEquals(0f, angles.frontalDeg, 0.05f)
    }

    @Test
    fun `phone tilted 5 degrees backward reads +5 degrees frontal`() {
        val rad = Math.toRadians(5.0)
        val g = Vector3(
            x = 0f,
            y = (9.81 * cos(rad)).toFloat(),
            z = (9.81 * sin(rad)).toFloat()
        )
        val angles = PlumbMath.computeVerticalAngles(g)!!
        assertEquals(0f, angles.lateralDeg, 0.05f)
        assertEquals(5f, angles.frontalDeg, 0.05f)
    }

    @Test
    fun `display rotation 90 remaps horizontal physical X to upright display Y`() {
        val raw = Vector3(9.81f, 0f, 0f)
        val remapped = PlumbMath.remapToDisplay(raw, Surface.ROTATION_90)
        val angles = PlumbMath.computeVerticalAngles(remapped)!!
        assertEquals(0f, angles.lateralDeg, 0.01f)
        assertEquals(0f, angles.frontalDeg, 0.01f)
    }

    @Test
    fun `calibration subtracts offset accurately`() {
        val raw = PlumbAngles(lateralDeg = 1.4f, frontalDeg = -0.8f)
        val calibrated = PlumbMath.applyCalibration(raw, lateralOffsetDeg = 1.2f, frontalOffsetDeg = -0.8f)
        assertEquals(0.2f, calibrated.lateralDeg, 0.01f)
        assertEquals(0.0f, calibrated.frontalDeg, 0.01f)
    }

    @Test
    fun `hysteresis prevents flickering right at tolerance boundary`() {
        val tolerance = 0.5f
        assertTrue(PlumbMath.isAxisAligned(0.48f, tolerance, wasAligned = false))
        assertFalse(PlumbMath.isAxisAligned(0.53f, tolerance, wasAligned = false))
        // Já estava alinhado -> tolera pequeno ruído até 0.5 + 0.08
        assertTrue(PlumbMath.isAxisAligned(0.55f, tolerance, wasAligned = true))
        assertFalse(PlumbMath.isAxisAligned(0.65f, tolerance, wasAligned = true))
    }

    @Test
    fun `invalid freefall vector returns null`() {
        assertNull(PlumbMath.computeVerticalAngles(Vector3(0.1f, 0.2f, 0.1f)))
    }
}
