package com.focus.app.feature.session

import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus

/**
 * Estado inmutable de la UI para la pantalla de sesión activa complementada con Smartwatch.
 *
 * Administra el progreso del tiempo, el tipo de actividad, la detección de sedentarismo
 * ("Llevas mucho tiempo sin moverte") y el modo de descanso guiado ("Te indica cuándo descansar").
 */
data class FocusSessionUiState(
    val session: FocusSession? = null,
    val activityType: FocusActivityType = FocusActivityType.TRABAJAR,
    val remainingMillis: Long = 0L,
    val timeRemainingLabel: String = "00:00",
    val progress: Float = 0f,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val restrictedAppsCount: Int = 0,
    val durationMinutes: Int = 45,
    val isSmartwatchSynced: Boolean = true,
    val isSedentaryAlertActive: Boolean = false,
    val minutesWithoutMovement: Int = 0,
    val sedentaryThresholdMinutes: Int = 45,
    val isBreakModeActive: Boolean = false,
    val breakRemainingMillis: Long = 0L,
    val breakRemainingLabel: String = "05:00",
    val breakDurationMinutes: Int = 5,
    val showEndConfirmationDialog: Boolean = false
) {
    val isCompleted: Boolean get() = status == SessionStatus.COMPLETED
    val isInterrupted: Boolean get() = status == SessionStatus.INTERRUPTED
    val isActive: Boolean get() = status == SessionStatus.ACTIVE
}
