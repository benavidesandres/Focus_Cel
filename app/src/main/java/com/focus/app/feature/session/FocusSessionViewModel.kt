package com.focus.app.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.usecase.EndFocusSessionUseCase
import com.focus.app.domain.usecase.ObserveFocusSessionUseCase
import com.focus.app.platform.notification.FocusNotificationHelper
import com.focus.app.platform.smartwatch.SmartwatchSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsable de la sesión activa y la coordinación con el Smartwatch.
 *
 * Mantiene la cuenta regresiva precisa, actualiza el reloj en tiempo real,
 * gestiona alertas de sedentarismo ("Mucho tiempo sin moverte") y el modo descanso ("Cuándo descansar").
 */
@HiltViewModel
class FocusSessionViewModel @Inject constructor(
    private val observeSession: ObserveFocusSessionUseCase,
    private val endSession: EndFocusSessionUseCase,
    private val notificationHelper: FocusNotificationHelper,
    val smartwatchSyncManager: SmartwatchSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusSessionUiState())
    val uiState: StateFlow<FocusSessionUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var breakJob: Job? = null
    private var currentSession: FocusSession? = null

    init {
        observeActiveSession()
        observeSedentarism()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            observeSession().collect { session ->
                currentSession = session
                if (session != null && session.status == SessionStatus.ACTIVE) {
                    val nowMs = System.currentTimeMillis()
                    if (session.isExpired(nowMs)) {
                        handleSessionExpiry(session)
                    } else {
                        updateStateForSession(session, nowMs)
                        startTicker()
                    }
                } else if (session != null && session.status == SessionStatus.COMPLETED) {
                    stopTicker()
                    _uiState.update {
                        it.copy(
                            session = session,
                            activityType = session.activityType,
                            status = SessionStatus.COMPLETED,
                            remainingMillis = 0L,
                            timeRemainingLabel = "00:00",
                            progress = 1f
                        )
                    }
                } else if (session != null && session.status == SessionStatus.INTERRUPTED) {
                    stopTicker()
                    _uiState.update {
                        it.copy(
                            session = session,
                            status = SessionStatus.INTERRUPTED
                        )
                    }
                }
            }
        }
    }

    private fun observeSedentarism() {
        viewModelScope.launch {
            smartwatchSyncManager.sedentarismDetector.status.collect { sedStatus ->
                _uiState.update {
                    it.copy(
                        isSedentaryAlertActive = sedStatus.isAlertActive,
                        minutesWithoutMovement = sedStatus.minutesWithoutMovement,
                        sedentaryThresholdMinutes = sedStatus.thresholdMinutes
                    )
                }
            }
        }
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return

        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                val session = currentSession ?: break
                val nowMs = System.currentTimeMillis()

                if (session.isExpired(nowMs)) {
                    handleSessionExpiry(session)
                    break
                } else {
                    updateStateForSession(session, nowMs)
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun updateStateForSession(session: FocusSession, nowMs: Long) {
        val remainingMs = session.remainingMillis(nowMs)
        val elapsedProgress = (1f - session.progressFraction(nowMs)).coerceIn(0f, 1f)
        val timeLabel = formatTime(remainingMs)

        notificationHelper.showSessionNotification("${session.activityType.displayName}: $timeLabel")

        // Sincroniza cada segundo con el Smartwatch
        smartwatchSyncManager.notifyProgress(
            remainingMillis = remainingMs,
            progressFraction = elapsedProgress,
            activityType = session.activityType
        )

        _uiState.update {
            it.copy(
                session = session,
                activityType = session.activityType,
                remainingMillis = remainingMs,
                timeRemainingLabel = timeLabel,
                progress = elapsedProgress,
                status = session.status,
                restrictedAppsCount = session.selectedPackageNames.size,
                durationMinutes = session.durationMinutes,
                isSmartwatchSynced = true
            )
        }
    }

    /**
     * Al expirar el tiempo de concentración, activa el modo descanso guiado
     * ("Te indica cuándo descansar") con alerta táctil en el Smartwatch.
     */
    private fun handleSessionExpiry(session: FocusSession) {
        stopTicker()
        notificationHelper.cancelSessionNotification()

        val breakMinutes = session.breakDurationMinutes.coerceAtLeast(5)
        smartwatchSyncManager.notifyRestTime(breakMinutes)

        _uiState.update {
            it.copy(
                isBreakModeActive = true,
                breakDurationMinutes = breakMinutes,
                breakRemainingMillis = breakMinutes * 60_000L,
                breakRemainingLabel = "%02d:00".format(breakMinutes),
                status = SessionStatus.COMPLETED,
                remainingMillis = 0L,
                timeRemainingLabel = "00:00",
                progress = 1f
            )
        }

        startBreakCountdown(breakMinutes)

        viewModelScope.launch {
            endSession.onExpiry(session.id, session.status)
        }
    }

    /**
     * Cuenta regresiva del descanso guiado.
     */
    private fun startBreakCountdown(breakMinutes: Int) {
        breakJob?.cancel()
        breakJob = viewModelScope.launch {
            var remainingSec = breakMinutes * 60
            while (isActive && remainingSec > 0) {
                delay(1_000L)
                remainingSec--
                val min = remainingSec / 60
                val sec = remainingSec % 60
                _uiState.update {
                    it.copy(
                        breakRemainingMillis = remainingSec * 1_000L,
                        breakRemainingLabel = "%02d:%02d".format(min, sec)
                    )
                }
            }
            // Descanso completado
            _uiState.update { it.copy(isBreakModeActive = false) }
        }
    }

    /**
     * El usuario confirma que se estiró o movió; se descarta la alerta de inactividad.
     */
    fun onAcknowledgeMovement() {
        smartwatchSyncManager.simulateMovement()
    }

    /**
     * Salta el descanso y concluye la sesión.
     */
    fun onSkipBreak() {
        breakJob?.cancel()
        _uiState.update { it.copy(isBreakModeActive = false) }
    }

    fun onEndSessionClicked() {
        _uiState.update { it.copy(showEndConfirmationDialog = true) }
    }

    fun onDismissEndSessionDialog() {
        _uiState.update { it.copy(showEndConfirmationDialog = false) }
    }

    fun onConfirmEndSession() {
        _uiState.update { it.copy(showEndConfirmationDialog = false) }
        val session = currentSession ?: return
        stopTicker()
        breakJob?.cancel()
        notificationHelper.cancelSessionNotification()
        smartwatchSyncManager.notifySessionStopped()

        viewModelScope.launch {
            endSession.manually(session.id)
            _uiState.update {
                it.copy(status = SessionStatus.INTERRUPTED)
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1_000L
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
        breakJob?.cancel()
    }
}
