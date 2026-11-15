package com.nivelpro.app.data

import android.content.Context

enum class SmoothingPreset(val label: String, val alpha: Float) {
    FAST("Rápida", 0.28f),
    BALANCED("Equilibrada", 0.16f),
    STABLE("Alta Estabilidade", 0.09f)
}

data class PlumbSettings(
    val lateralOffsetDeg: Float = 0f,
    val frontalOffsetDeg: Float = 0f,
    val hasCustomCalibration: Boolean = false,
    val toleranceDeg: Float = 0.5f,
    val beepEnabled: Boolean = true,
    val smoothingPreset: SmoothingPreset = SmoothingPreset.BALANCED
)

class PreferencesRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): PlumbSettings {
        val presetName = prefs.getString(KEY_SMOOTHING, SmoothingPreset.BALANCED.name)
        val preset = SmoothingPreset.entries.firstOrNull { it.name == presetName }
            ?: SmoothingPreset.BALANCED
        return PlumbSettings(
            lateralOffsetDeg = prefs.getFloat(KEY_LATERAL_OFFSET, 0f),
            frontalOffsetDeg = prefs.getFloat(KEY_FRONTAL_OFFSET, 0f),
            hasCustomCalibration = prefs.getBoolean(KEY_HAS_CALIBRATION, false),
            toleranceDeg = prefs.getFloat(KEY_TOLERANCE, 0.5f).coerceIn(0.1f, 3.0f),
            beepEnabled = prefs.getBoolean(KEY_BEEP_ENABLED, true),
            smoothingPreset = preset
        )
    }

    fun saveCalibration(lateralOffsetDeg: Float, frontalOffsetDeg: Float) {
        prefs.edit()
            .putFloat(KEY_LATERAL_OFFSET, lateralOffsetDeg)
            .putFloat(KEY_FRONTAL_OFFSET, frontalOffsetDeg)
            .putBoolean(KEY_HAS_CALIBRATION, true)
            .apply()
    }

    fun clearCalibration() {
        prefs.edit()
            .putFloat(KEY_LATERAL_OFFSET, 0f)
            .putFloat(KEY_FRONTAL_OFFSET, 0f)
            .putBoolean(KEY_HAS_CALIBRATION, false)
            .apply()
    }

    fun saveTolerance(toleranceDeg: Float) {
        prefs.edit()
            .putFloat(KEY_TOLERANCE, toleranceDeg.coerceIn(0.1f, 3.0f))
            .apply()
    }

    fun saveBeepEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BEEP_ENABLED, enabled)
            .apply()
    }

    fun saveSmoothingPreset(preset: SmoothingPreset) {
        prefs.edit()
            .putString(KEY_SMOOTHING, preset.name)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "nivel_pro_prefs"
        const val KEY_LATERAL_OFFSET = "lateral_offset_deg"
        const val KEY_FRONTAL_OFFSET = "frontal_offset_deg"
        const val KEY_HAS_CALIBRATION = "has_custom_calibration"
        const val KEY_TOLERANCE = "tolerance_deg"
        const val KEY_BEEP_ENABLED = "beep_enabled"
        const val KEY_SMOOTHING = "smoothing_preset"
    }
}
