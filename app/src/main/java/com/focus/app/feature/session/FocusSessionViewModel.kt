package com.focus.app.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.core.util.formatCountdown
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionPhase
import com.focus.app.domain.model.SmartwatchDevice
import com.focus.app.domain.repository.SmartwatchGateway
import com.focus.app.domain.session.FocusSessionManager
import com.focus.app.domain.session.InactivityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel de la pantalla de sesión.
 * No contiene lógica de temporización: traduce a UI el estado de [FocusSessionManager],
 * que sigue funcionando aunque esta pantalla se destruya.
 */
@HiltViewModel
class FocusSessionViewModel @Inject constructor(
    private val sessionManager: FocusSessionManager,
    smartwatch: SmartwatchGateway
) : ViewModel() {

    private val showEndDialog = MutableStateFlow(false)

    val uiState: StateFlow<FocusSessionUiState> = combine(
        sessionManager.phase,
        sessionManager.inactivityStatus,
        smartwatch.device,
        showEndDialog,
        ::toUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = toUiState(
            phase = sessionManager.phase.value,
            inactivity = sessionManager.inactivityStatus.value,
            device = smartwatch.device.value,
            isEndDialogVisible = false
        )
    )

    /** El usuario confirma que se estiró o se movió. */
    fun onAcknowledgeMovement() {
        sessionManager.acknowledgeMovement()
    }

    fun onSkipBreak() {
        sessionManager.skipBreak()
    }

    fun onDone() {
        sessionManager.acknowledgeCompletion()
    }

    fun onEndSessionClicked() {
        showEndDialog.value = true
    }

    fun onDismissEndSessionDialog() {
        showEndDialog.value = false
    }

    fun onConfirmEndSession() {
        showEndDialog.value = false
        sessionManager.stop()
    }

    private fun toUiState(
        phase: SessionPhase,
        inactivity: InactivityStatus,
        device: SmartwatchDevice,
        isEndDialogVisible: Boolean
    ): FocusSessionUiState {
        val base = FocusSessionUiState(
            isWatchConnected = device.isConnected,
            isInactivityAlertActive = inactivity.isAlertActive,
            minutesWithoutMovement = inactivity.minutesWithoutMovement,
            showEndConfirmationDialog = isEndDialogVisible
        )

        return when (phase) {
            SessionPhase.Idle -> base
            is SessionPhase.Focusing -> base.copy(
                stage = SessionStage.FOCUSING,
                activityType = phase.session.activityType,
                timeRemainingLabel = formatCountdown(phase.remainingMillis),
                progress = elapsedFraction(phase.session, phase.remainingMillis)
            )
            is SessionPhase.Resting -> base.copy(
                stage = SessionStage.RESTING,
                activityType = phase.session.activityType,
                progress = 1f,
                breakRemainingLabel = formatCountdown(phase.remainingMillis)
            )
            is SessionPhase.Completed -> base.copy(
                stage = SessionStage.COMPLETED,
                activityType = phase.session.activityType,
                progress = 1f
            )
        }
    }

    private fun elapsedFraction(session: FocusSession, remainingMillis: Long): Float {
        val totalMillis = session.endTimestampMs - session.startTimestampMs
        if (totalMillis <= 0L) return 1f
        return (1f - remainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
