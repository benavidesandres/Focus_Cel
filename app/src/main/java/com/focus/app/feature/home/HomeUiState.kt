package com.focus.app.feature.home

import com.focus.app.domain.model.SessionStatus

/**
 * Estado inmutable de la UI de la pantalla principal.
 * Representa todas las variaciones posibles de la pantalla de inicio.
 *
 * @param selectedAppCount Número de aplicaciones seleccionadas para restricción.
 * @param selectedDurationMinutes Duración de sesión seleccionada en minutos.
 * @param durationOptions Opciones disponibles de duración en minutos.
 * @param isAccessibilityServiceEnabled Si el servicio de accesibilidad está activo.
 * @param activeSessionStatus Estado de la sesión activa (null si no hay sesión).
 * @param isLoading Si la pantalla está cargando datos iniciales.
 * @param errorMessage Mensaje de error a mostrar, o null si no hay error.
 */
data class HomeUiState(
    val selectedAppCount: Int = 0,
    val selectedDurationMinutes: Int = 25,
    val durationOptions: List<Int> = listOf(25, 45, 60, 90),
    val isAccessibilityServiceEnabled: Boolean = false,
    val activeSessionStatus: SessionStatus? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    /**
     * Indica si el usuario puede iniciar una sesión de restricción real.
     * Requiere al menos una app seleccionada Y el servicio de accesibilidad habilitado.
     */
    val canStartRestrictedSession: Boolean
        get() = selectedAppCount > 0 && isAccessibilityServiceEnabled

    /**
     * Indica si el usuario puede iniciar una sesión solo de temporizador
     * (sin restricciones reales porque el servicio no está habilitado o no hay apps).
     */
    val canStartTimerOnly: Boolean
        get() = selectedDurationMinutes > 0

    /**
     * Indica si hay una sesión activa en progreso.
     */
    val hasActiveSession: Boolean
        get() = activeSessionStatus == SessionStatus.ACTIVE

    /**
     * Mensaje de estado para mostrar en la tarjeta principal.
     */
    val sessionReadinessMessage: String
        get() = when {
            selectedAppCount == 0 -> "Select apps to restrict"
            !isAccessibilityServiceEnabled -> "Enable accessibility service to restrict apps"
            else -> "Ready to focus"
        }
}
