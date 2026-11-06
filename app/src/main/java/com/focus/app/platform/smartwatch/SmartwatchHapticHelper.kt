package com.focus.app.platform.smartwatch

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.focus.app.domain.model.SmartwatchProtocol
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reproduce en el teléfono los patrones de vibración que también se envían al reloj,
 * para que el usuario reciba retroalimentación táctil sin mirar la pantalla.
 */
@Singleton
class SmartwatchHapticHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    /**
     * Ejecuta el patrón háptico especificado.
     *
     * @param pattern El patrón de vibración a reproducir.
     */
    fun play(pattern: SmartwatchProtocol.HapticPattern) {
        val deviceVibrator = vibrator?.takeIf { it.hasVibrator() } ?: return

        val timings = pattern.timingsMs
        val amplitudes = IntArray(timings.size) { index ->
            if (index % 2 == 0) 0 else VIBRATION_AMPLITUDE
        }
        deviceVibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, NO_REPEAT))
    }

    private companion object {
        const val VIBRATION_AMPLITUDE = 220
        const val NO_REPEAT = -1
    }
}
