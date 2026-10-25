package com.focus.app.feature.home

import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.model.SmartwatchDevice

/**
 * Estado inmutable de la UI de la pantalla principal adaptada para Smartwatch Companion.
 *
 * @param selectedActivity Tipo de actividad elegida ("¿Qué vas a hacer?").
 * @param selectedAppCount Número de aplicaciones seleccionadas para restricción.
 * @param selectedDurationMinutes Duración de sesión seleccionada en minutos (por defecto 45 min).
 * @param durationOptions Opciones disponibles de duración en minutos.
 * @param isAccessibilityServiceEnabled Si el servicio de accesibilidad del teléfono está activo.
 * @param activeSessionStatus Estado de la sesión activa (null si no hay sesión).
 * @param smartwatchDevice Estado del Smartwatch sincronizado (Wear OS).
 * @param isLoading Si la pantalla está cargando datos iniciales.
 * @param errorMessage Mensaje de error a mostrar, o null si no hay error.
 */
data class HomeUiState(
    val selectedActivity: FocusActivityType = FocusActivityType.TRABAJAR,
    val selectedAppCount: Int = 0,
    val selectedDurationMinutes: Int = 45,
    val durationOptions: List<Int> = listOf(25, 45, 60, 90),
    val isAccessibilityServiceEnabled: Boolean = false,
    val activeSessionStatus: SessionStatus? = null,
    val smartwatchDevice: SmartwatchDevice = SmartwatchDevice(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    /**
     * Indica si el usuario puede iniciar una sesión de restricción real con apps bloqueadas.
     */
    val canStartRestrictedSession: Boolean
        get() = selectedAppCount > 0 && isAccessibilityServiceEnabled

    /**
     * Indica si el usuario puede iniciar una sesión de temporizador / smartwatch focus.
     */
    val canStartTimerOnly: Boolean
        get() = selectedDurationMinutes > 0

    /**
     * Indica si hay una sesión activa en progreso.
     */
    val hasActiveSession: Boolean
        get() = activeSessionStatus == SessionStatus.ACTIVE

    /**
     * Mensaje de estado para mostrar en la tarjeta de preparación.
     */
    val sessionReadinessMessage: String
        get() = when {
            smartwatchDevice.isConnected && selectedAppCount > 0 && isAccessibilityServiceEnabled ->
                "Listo: Reloj y Celular sincronizados para ${selectedActivity.displayName}"
            smartwatchDevice.isConnected && selectedAppCount == 0 ->
                "Listo: Modo Zen en Smartwatch (${selectedActivity.displayName})"
            !isAccessibilityServiceEnabled ->
                "Activa accesibilidad para restringir apps en el celular"
            else -> "Listo para concentrarte"
        }
}
