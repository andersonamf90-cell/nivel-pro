package com.nivelpro.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nivelpro.app.audio.PlumbTonePlayer
import com.nivelpro.app.data.PlumbSettings
import com.nivelpro.app.data.PreferencesRepository
import com.nivelpro.app.data.SmoothingPreset
import com.nivelpro.app.sensor.ActiveSensorSource
import com.nivelpro.app.sensor.FrontalDirection
import com.nivelpro.app.sensor.LateralDirection
import com.nivelpro.app.sensor.PlumbAngles
import com.nivelpro.app.sensor.PlumbMath
import com.nivelpro.app.sensor.PlumbSensorManager
import com.nivelpro.app.sensor.SensorAccuracyLevel
import com.nivelpro.app.sensor.SensorReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlumbUiState(
    val lateralDeg: Float = 0f,
    val frontalDeg: Float = 0f,
    val rawLateralDeg: Float = 0f,
    val rawFrontalDeg: Float = 0f,
    val combinedDeviationDeg: Float = 0f,
    val isLateralAligned: Boolean = false,
    val isFrontalAligned: Boolean = false,
    val isFullyPlumb: Boolean = false,
    val lateralDirection: LateralDirection = LateralDirection.ALIGNED,
    val frontalDirection: FrontalDirection = FrontalDirection.ALIGNED,
    val isFrozen: Boolean = false,
    val isCalibrating: Boolean = false,
    val hasCustomCalibration: Boolean = false,
    val lateralOffsetDeg: Float = 0f,
    val frontalOffsetDeg: Float = 0f,
    val toleranceDeg: Float = 0.5f,
    val beepEnabled: Boolean = true,
    val smoothingPreset: SmoothingPreset = SmoothingPreset.BALANCED,
    val sensorSource: ActiveSensorSource = ActiveSensorSource.UNAVAILABLE,
    val accuracyLevel: SensorAccuracyLevel = SensorAccuracyLevel.HIGH,
    val hasValidSample: Boolean = false,
    val showCalibrationDialog: Boolean = false,
    val showSettingsSheet: Boolean = false
)

class PlumbViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)
    private val sensorManager = PlumbSensorManager(application)
    private val tonePlayer = PlumbTonePlayer()

    private var settings: PlumbSettings = repository.load()
    private var previousLateralAligned = false
    private var previousFrontalAligned = false
    private var previousFullyPlumb = false

    private val _uiState = MutableStateFlow(
        PlumbUiState(
            hasCustomCalibration = settings.hasCustomCalibration,
            lateralOffsetDeg = settings.lateralOffsetDeg,
            frontalOffsetDeg = settings.frontalOffsetDeg,
            toleranceDeg = settings.toleranceDeg,
            beepEnabled = settings.beepEnabled,
            smoothingPreset = settings.smoothingPreset,
            sensorSource = sensorManager.reading.value.sensorSource
        )
    )
    val uiState: StateFlow<PlumbUiState> = _uiState.asStateFlow()

    init {
        sensorManager.smoothingAlpha = settings.smoothingPreset.alpha
        viewModelScope.launch {
            sensorManager.reading.collect { reading ->
                processReading(reading)
            }
        }
    }

    fun startSensors() {
        sensorManager.start()
    }

    fun stopSensors() {
        sensorManager.stop()
    }

    private fun processReading(reading: SensorReading) {
        if (_uiState.value.isFrozen) {
            _uiState.update {
                it.copy(
                    sensorSource = reading.sensorSource,
                    accuracyLevel = reading.accuracyLevel
                )
            }
            return
        }

        val calibrated = PlumbMath.applyCalibration(
            raw = reading.filteredAngles,
            lateralOffsetDeg = settings.lateralOffsetDeg,
            frontalOffsetDeg = settings.frontalOffsetDeg
        )

        val latAligned = reading.hasValidSample && PlumbMath.isAxisAligned(
            angleDeg = calibrated.lateralDeg,
            toleranceDeg = settings.toleranceDeg,
            wasAligned = previousLateralAligned
        )
        val frontAligned = reading.hasValidSample && PlumbMath.isAxisAligned(
            angleDeg = calibrated.frontalDeg,
            toleranceDeg = settings.toleranceDeg,
            wasAligned = previousFrontalAligned
        )
        val fullyPlumb = latAligned && frontAligned

        if (fullyPlumb && !previousFullyPlumb && settings.beepEnabled && !_uiState.value.isCalibrating) {
            tonePlayer.playLevelChime()
        }

        previousLateralAligned = latAligned
        previousFrontalAligned = frontAligned
        previousFullyPlumb = fullyPlumb

        _uiState.update { state ->
            state.copy(
                lateralDeg = calibrated.lateralDeg,
                frontalDeg = calibrated.frontalDeg,
                rawLateralDeg = reading.filteredAngles.lateralDeg,
                rawFrontalDeg = reading.filteredAngles.frontalDeg,
                combinedDeviationDeg = PlumbMath.combinedDeviationDeg(
                    calibrated.lateralDeg,
                    calibrated.frontalDeg
                ),
                isLateralAligned = latAligned,
                isFrontalAligned = frontAligned,
                isFullyPlumb = fullyPlumb,
                lateralDirection = PlumbMath.lateralDirection(
                    calibrated.lateralDeg,
                    settings.toleranceDeg,
                    latAligned
                ),
                frontalDirection = PlumbMath.frontalDirection(
                    calibrated.frontalDeg,
                    settings.toleranceDeg,
                    frontAligned
                ),
                sensorSource = reading.sensorSource,
                accuracyLevel = reading.accuracyLevel,
                hasValidSample = reading.hasValidSample
            )
        }
    }

    fun toggleFreeze() {
        _uiState.update { it.copy(isFrozen = !it.isFrozen) }
    }

    fun toggleBeep() {
        val next = !settings.beepEnabled
        settings = settings.copy(beepEnabled = next)
        repository.saveBeepEnabled(next)
        _uiState.update { it.copy(beepEnabled = next) }
    }

    fun updateTolerance(toleranceDeg: Float) {
        val clamped = ((toleranceDeg * 10f).toInt() / 10f).coerceIn(0.1f, 3.0f)
        settings = settings.copy(toleranceDeg = clamped)
        repository.saveTolerance(clamped)
        _uiState.update { it.copy(toleranceDeg = clamped) }
    }

    fun updateSmoothingPreset(preset: SmoothingPreset) {
        settings = settings.copy(smoothingPreset = preset)
        sensorManager.smoothingAlpha = preset.alpha
        repository.saveSmoothingPreset(preset)
        _uiState.update { it.copy(smoothingPreset = preset) }
    }

    fun calibrateZero() {
        if (_uiState.value.isFrozen) {
            _uiState.update { it.copy(isFrozen = false) }
        }
        _uiState.update { it.copy(isCalibrating = true) }
        sensorManager.requestAveragedCalibration(sampleCount = 25) { latOffset, frontOffset ->
            settings = settings.copy(
                lateralOffsetDeg = latOffset,
                frontalOffsetDeg = frontOffset,
                hasCustomCalibration = true
            )
            repository.saveCalibration(latOffset, frontOffset)
            _uiState.update {
                it.copy(
                    isCalibrating = false,
                    hasCustomCalibration = true,
                    lateralOffsetDeg = latOffset,
                    frontalOffsetDeg = frontOffset
                )
            }
        }
    }

    fun resetCalibration() {
        settings = settings.copy(
            lateralOffsetDeg = 0f,
            frontalOffsetDeg = 0f,
            hasCustomCalibration = false
        )
        repository.clearCalibration()
        _uiState.update {
            it.copy(
                hasCustomCalibration = false,
                lateralOffsetDeg = 0f,
                frontalOffsetDeg = 0f
            )
        }
    }

    fun openCalibrationDialog() {
        _uiState.update { it.copy(showCalibrationDialog = true) }
    }

    fun closeCalibrationDialog() {
        _uiState.update { it.copy(showCalibrationDialog = false) }
    }

    fun openSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = true) }
    }

    fun closeSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = false) }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stop()
        tonePlayer.release()
    }
}
