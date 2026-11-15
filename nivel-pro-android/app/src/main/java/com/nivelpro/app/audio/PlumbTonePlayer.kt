package com.nivelpro.app.audio

import android.media.AudioManager
import android.media.ToneGenerator

class PlumbTonePlayer {

    private var toneGenerator: ToneGenerator? = null
    private var lastBeepTimeMs: Long = 0L

    private fun ensureGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            toneGenerator = try {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
            } catch (_: RuntimeException) {
                null
            }
        }
        return toneGenerator
    }

    fun playLevelChime() {
        val now = System.currentTimeMillis()
        if (now - lastBeepTimeMs < BEEP_COOLDOWN_MS) return
        lastBeepTimeMs = now
        try {
            ensureGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 110)
        } catch (_: RuntimeException) {
            // Ignora falhas transitórias caso o dispositivo esteja em modo silencioso estrito
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (_: RuntimeException) {
            // Sem ação necessária
        } finally {
            toneGenerator = null
        }
    }

    private companion object {
        const val BEEP_COOLDOWN_MS = 650L
    }
}
