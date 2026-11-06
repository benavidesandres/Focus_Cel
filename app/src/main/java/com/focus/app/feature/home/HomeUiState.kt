package com.focus.app.feature.home

import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.SmartwatchDevice

/**
 * Qué protección tendrá la sesión si se inicia con la configuración actual.
 */
enum class SessionReadiness {
    /** El teléfono bloqueará las apps y el reloj está conectado. */
    READY,

    /** El teléfono bloqueará las apps, pero no hay reloj conectado. */
    READY_WITHOUT_WATCH,

    /** Hay apps seleccionadas, pero falta activar el servicio de accesibilidad. */
    NEEDS_ACCESSIBILITY,

    /** No hay apps seleccionadas: solo correrá el temporizador. */
    TIMER_ONLY
}

/**
 * Estado inmutable de la UI de la pantalla principal.
 *
 * @param selectedActivity Tipo de actividad elegida ("What are you going to do?").
 * @param selectedAppCount Número de aplicaciones seleccionadas para restricción.
 * @param selectedDurationMinutes Duración de sesión seleccionada en minutos.
 * @param durationOptions Opciones disponibles de duración en minutos.
 * @param isAccessibilityServiceEnabled Si el servicio de accesibilidad del teléfono está activo.
 * @param hasOngoingSession Si hay una sesión, descanso o resumen pendiente de mostrar.
 * @param smartwatchDevice Estado real del reloj.
 * @param errorMessage Mensaje de error a mostrar, o null si no hay error.
 */
data class HomeUiState(
    val selectedActivity: FocusActivityType = FocusActivityType.WORK,
    val selectedAppCount: Int = 0,
    val selectedDurationMinutes: Int = 45,
    val durationOptions: List<Int> = listOf(25, 45, 60, 90),
    val isAccessibilityServiceEnabled: Boolean = false,
    val hasOngoingSession: Boolean = false,
    val smartwatchDevice: SmartwatchDevice = SmartwatchDevice(),
    val errorMessage: String? = null
) {
    /** Protección que ofrecerá la sesión con la configuración actual. */
    val readiness: SessionReadiness
        get() = when {
            selectedAppCount == 0 -> SessionReadiness.TIMER_ONLY
            !isAccessibilityServiceEnabled -> SessionReadiness.NEEDS_ACCESSIBILITY
            smartwatchDevice.isConnected -> SessionReadiness.READY
            else -> SessionReadiness.READY_WITHOUT_WATCH
        }
}
