package com.focus.app.domain.model

/**
 * Preferencias del usuario sobre el comportamiento del reloj.
 *
 * @param hapticsEnabled Si se envían vibraciones hápticas.
 * @param inactivityAlertsEnabled Si se avisa de la inactividad física prolongada.
 * @param inactivityThresholdMinutes Minutos sin movimiento antes de avisar.
 * @param restIndicatorEnabled Si se inicia un descanso guiado al terminar la sesión.
 */
data class SmartwatchSettings(
    val hapticsEnabled: Boolean = true,
    val inactivityAlertsEnabled: Boolean = true,
    val inactivityThresholdMinutes: Int = DEFAULT_INACTIVITY_THRESHOLD_MINUTES,
    val restIndicatorEnabled: Boolean = true
) {
    companion object {
        const val DEFAULT_INACTIVITY_THRESHOLD_MINUTES = 45
    }
}
