package com.nivelpro.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ActiveSensorSource(val label: String) {
    GRAVITY("TYPE_GRAVITY (Fusão IMU)"),
    ACCELEROMETER("TYPE_ACCELEROMETER (Fallback)"),
    UNAVAILABLE("Sensor indisponível")
}

enum class SensorAccuracyLevel(val label: String) {
    HIGH("Alta"),
    MEDIUM("Média"),
    LOW("Baixa"),
    UNRELIABLE("Instável")
}

data class SensorReading(
    val rawAngles: PlumbAngles = PlumbAngles(0f, 0f),
    val filteredAngles: PlumbAngles = PlumbAngles(0f, 0f),
    val sensorSource: ActiveSensorSource = ActiveSensorSource.UNAVAILABLE,
    val accuracyLevel: SensorAccuracyLevel = SensorAccuracyLevel.HIGH,
    val surfaceRotation: Int = Surface.ROTATION_0,
    val hasValidSample: Boolean = false
)

class PlumbSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val accelerometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val activeSensor: Sensor? = gravitySensor ?: accelerometerSensor

    private val _reading = MutableStateFlow(
        SensorReading(
            sensorSource = when {
                gravitySensor != null -> ActiveSensorSource.GRAVITY
                accelerometerSensor != null -> ActiveSensorSource.ACCELEROMETER
                else -> ActiveSensorSource.UNAVAILABLE
            }
        )
    )
    val reading: StateFlow<SensorReading> = _reading.asStateFlow()

    @Volatile
    var smoothingAlpha: Float = 0.16f

    private var filteredVector: Vector3? = null
    private var currentAccuracy: SensorAccuracyLevel = SensorAccuracyLevel.HIGH
    private var isListening = false

    // Acumulador para calibração com média de múltiplas amostras reais
    private var calibrationSamplesTarget = 0
    private val calibrationLateralSamples = mutableListOf<Float>()
    private val calibrationFrontalSamples = mutableListOf<Float>()
    private var onCalibrationComplete: ((Float, Float) -> Unit)? = null

    fun start() {
        val sensor = activeSensor ?: return
        if (isListening) return
        filteredVector = null
        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    fun requestAveragedCalibration(
        sampleCount: Int = 25,
        onComplete: (lateralOffsetDeg: Float, frontalOffsetDeg: Float) -> Unit
    ) {
        calibrationLateralSamples.clear()
        calibrationFrontalSamples.clear()
        calibrationSamplesTarget = sampleCount
        onCalibrationComplete = onComplete
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.values.size < 3) return

        val rotation = currentDisplayRotation()
        val rawDisplay = PlumbMath.remapToDisplay(
            raw = Vector3(event.values[0], event.values[1], event.values[2]),
            surfaceRotation = rotation
        )

        val smoothed = PlumbMath.lowPassFilter(
            current = rawDisplay,
            previous = filteredVector,
            alpha = smoothingAlpha
        )
        filteredVector = smoothed

        val rawAngles = PlumbMath.computeVerticalAngles(rawDisplay) ?: return
        val filteredAngles = PlumbMath.computeVerticalAngles(smoothed) ?: return

        if (calibrationSamplesTarget > 0) {
            calibrationLateralSamples.add(filteredAngles.lateralDeg)
            calibrationFrontalSamples.add(filteredAngles.frontalDeg)
            if (calibrationLateralSamples.size >= calibrationSamplesTarget) {
                val avgLat = circularMeanDegrees(calibrationLateralSamples)
                val avgFront = calibrationFrontalSamples.average().toFloat()
                val callback = onCalibrationComplete
                calibrationSamplesTarget = 0
                onCalibrationComplete = null
                calibrationLateralSamples.clear()
                calibrationFrontalSamples.clear()
                callback?.invoke(avgLat, avgFront)
            }
        }

        val source = when (event.sensor.type) {
            Sensor.TYPE_GRAVITY -> ActiveSensorSource.GRAVITY
            Sensor.TYPE_ACCELEROMETER -> ActiveSensorSource.ACCELEROMETER
            else -> ActiveSensorSource.UNAVAILABLE
        }

        _reading.value = SensorReading(
            rawAngles = rawAngles,
            filteredAngles = filteredAngles,
            sensorSource = source,
            accuracyLevel = currentAccuracy,
            surfaceRotation = rotation,
            hasValidSample = true
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        currentAccuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracyLevel.HIGH
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracyLevel.MEDIUM
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracyLevel.LOW
            else -> SensorAccuracyLevel.UNRELIABLE
        }
    }

    @Suppress("DEPRECATION")
    private fun currentDisplayRotation(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val displayManager =
                    context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation
                    ?: Surface.ROTATION_0
            } else {
                val windowManager =
                    context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }
        } catch (_: Exception) {
            Surface.ROTATION_0
        }
    }

    private fun circularMeanDegrees(anglesDeg: List<Float>): Float {
        if (anglesDeg.isEmpty()) return 0f
        var sumSin = 0.0
        var sumCos = 0.0
        for (deg in anglesDeg) {
            val rad = Math.toRadians(deg.toDouble())
            sumSin += kotlin.math.sin(rad)
            sumCos += kotlin.math.cos(rad)
        }
        return Math.toDegrees(kotlin.math.atan2(sumSin, sumCos)).toFloat()
    }
}
