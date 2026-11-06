package com.focus.app.feature.session

import com.focus.app.domain.model.FocusActivityType

/**
 * Etapa de la sesión que muestra la pantalla.
 */
enum class SessionStage {
    IDLE,
    FOCUSING,
    RESTING,
    COMPLETED
}

/**
 * Estado inmutable de la UI para la pantalla de sesión activa.
 *
 * @param stage Etapa actual; en [SessionStage.IDLE] la pantalla debe cerrarse.
 * @param activityType Actividad elegida.
 * @param timeRemainingLabel Tiempo de concentración restante.
 * @param progress Fracción transcurrida de la sesión (0f..1f).
 * @param breakRemainingLabel Tiempo de descanso restante.
 * @param isWatchConnected Si el reloj está conectado realmente.
 * @param isInactivityAlertActive Si se debe mostrar la alerta de inactividad.
 * @param minutesWithoutMovement Minutos sin movimiento detectado.
 * @param showEndConfirmationDialog Si se muestra la confirmación para terminar.
 */
data class FocusSessionUiState(
    val stage: SessionStage = SessionStage.IDLE,
    val activityType: FocusActivityType = FocusActivityType.WORK,
    val timeRemainingLabel: String = "00:00",
    val progress: Float = 0f,
    val breakRemainingLabel: String = "00:00",
    val isWatchConnected: Boolean = false,
    val isInactivityAlertActive: Boolean = false,
    val minutesWithoutMovement: Int = 0,
    val showEndConfirmationDialog: Boolean = false
)
