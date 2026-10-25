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
 * Gestor del motor háptico para la experiencia de concentración.
 * Ejecuta patrones de vibración tanto en el teléfono móvil como
 * coordinándolos hacia el smartwatch para que el usuario reciba
 * retroalimentación táctil sin mirar la pantalla constantemente.
 */
@Singleton
class SmartwatchHapticHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Ejecuta el patrón háptico especificado.
     *
     * @param pattern El patrón de vibración definido en [SmartwatchProtocol.HapticPattern].
     */
    fun playPattern(pattern: SmartwatchProtocol.HapticPattern) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        val timings = pattern.patternMs
        val amplitudes = IntArray(timings.size) { index ->
            if (index % 2 == 0) 0 else 220
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Exception) {
            // Manejo seguro en caso de restricciones de hardware
        }
    }

    /**
     * Vibración háptica al iniciar la sesión de concentración (Doble pulso).
     */
    fun onSessionStart() {
        playPattern(SmartwatchProtocol.HapticPattern.SESSION_START)
    }

    /**
     * Vibración suave al alcanzar la mitad del tiempo de enfoque.
     */
    fun onHalfwayMilestone() {
        playPattern(SmartwatchProtocol.HapticPattern.HALFWAY_MILESTONE)
    }

    /**
     * Vibración especial cuando el smartwatch detecta inactividad prolongada ("Mucho tiempo sin moverte").
     */
    fun onInactivityAlert() {
        playPattern(SmartwatchProtocol.HapticPattern.INACTIVITY_NUDGE)
    }

    /**
     * Vibración reconfortante al terminar el tiempo indicando momento de descansar.
     */
    fun onRestTimeAlert() {
        playPattern(SmartwatchProtocol.HapticPattern.REST_TIME)
    }
}
