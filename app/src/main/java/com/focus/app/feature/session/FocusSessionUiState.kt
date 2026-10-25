package com.focus.app.feature.session

import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus

/**
 * Estado inmutable de la UI para la pantalla de sesión de enfoque activa.
 *
 * @param session Sesión activa actual, o null si no existe.
 * @param remainingMillis Tiempo restante en milisegundos.
 * @param timeRemainingLabel Tiempo restante formateado en formato "MM:SS".
 * @param progress Fracción transcurrida de la sesión (0f al inicio, 1f al completar).
 * @param status Estado del ciclo de vida de la sesión (ACTIVE, COMPLETED, INTERRUPTED).
 * @param restrictedAppsCount Número de aplicaciones restringidas en esta sesión.
 * @param durationMinutes Duración total de la sesión en minutos.
 * @param showEndConfirmationDialog Si debe mostrarse el diálogo de confirmación para terminar la sesión.
 */
data class FocusSessionUiState(
    val session: FocusSession? = null,
    val remainingMillis: Long = 0L,
    val timeRemainingLabel: String = "00:00",
    val progress: Float = 0f,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val restrictedAppsCount: Int = 0,
    val durationMinutes: Int = 25,
    val showEndConfirmationDialog: Boolean = false
) {
    /** true si la sesión finalizó por expiración del temporizador. */
    val isCompleted: Boolean get() = status == SessionStatus.COMPLETED

    /** true si la sesión fue cancelada o interrumpida manualmente. */
    val isInterrupted: Boolean get() = status == SessionStatus.INTERRUPTED

    /** true si la sesión está en curso. */
    val isActive: Boolean get() = status == SessionStatus.ACTIVE
}
