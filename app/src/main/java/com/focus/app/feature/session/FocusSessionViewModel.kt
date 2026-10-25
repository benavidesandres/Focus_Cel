package com.focus.app.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.usecase.EndFocusSessionUseCase
import com.focus.app.domain.usecase.ObserveFocusSessionUseCase
import com.focus.app.platform.notification.FocusNotificationHelper
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
 * ViewModel responsable de la pantalla de sesión de enfoque activa.
 *
 * Mantiene un temporizador de cuenta regresiva basado en timestamps absolutos para
 * garantizar exactitud ante pausas del sistema, minimización de la app o cambio de orientación.
 * Comunica el estado de la sesión, actualiza la notificación en segundo plano y delega la finalización.
 */
@HiltViewModel
class FocusSessionViewModel @Inject constructor(
    private val observeSession: ObserveFocusSessionUseCase,
    private val endSession: EndFocusSessionUseCase,
    private val notificationHelper: FocusNotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusSessionUiState())
    val uiState: StateFlow<FocusSessionUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var currentSession: FocusSession? = null

    init {
        observeActiveSession()
    }

    /**
     * Observa la sesión activa desde el repositorio reactivo y gestiona el ciclo del temporizador.
     */
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

    /**
     * Inicia el bucle de actualización periódica del temporizador cada segundo.
     */
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

    /**
     * Detiene el trabajo del temporizador.
     */
    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    /**
     * Actualiza el estado de la UI con el tiempo restante y progreso actuales.
     */
    private fun updateStateForSession(session: FocusSession, nowMs: Long) {
        val remainingMs = session.remainingMillis(nowMs)
        val elapsedProgress = (1f - session.progressFraction(nowMs)).coerceIn(0f, 1f)
        val timeLabel = formatTime(remainingMs)

        notificationHelper.showSessionNotification(timeLabel)

        _uiState.update {
            it.copy(
                session = session,
                remainingMillis = remainingMs,
                timeRemainingLabel = timeLabel,
                progress = elapsedProgress,
                status = session.status,
                restrictedAppsCount = session.selectedPackageNames.size,
                durationMinutes = session.durationMinutes
            )
        }
    }

    /**
     * Gestiona la expiración natural del tiempo de la sesión.
     */
    private fun handleSessionExpiry(session: FocusSession) {
        stopTicker()
        notificationHelper.cancelSessionNotification()
        viewModelScope.launch {
            endSession.onExpiry(session.id, session.status)
            _uiState.update {
                it.copy(
                    status = SessionStatus.COMPLETED,
                    remainingMillis = 0L,
                    timeRemainingLabel = "00:00",
                    progress = 1f
                )
            }
        }
    }

    /**
     * Muestra el diálogo de confirmación para interrumpir la sesión.
     */
    fun onEndSessionClicked() {
        _uiState.update { it.copy(showEndConfirmationDialog = true) }
    }

    /**
     * Cierra el diálogo de confirmación sin terminar la sesión.
     */
    fun onDismissEndSessionDialog() {
        _uiState.update { it.copy(showEndConfirmationDialog = false) }
    }

    /**
     * Confirma la finalización manual de la sesión por parte del usuario.
     */
    fun onConfirmEndSession() {
        _uiState.update { it.copy(showEndConfirmationDialog = false) }
        val session = currentSession ?: return
        stopTicker()
        notificationHelper.cancelSessionNotification()
        viewModelScope.launch {
            endSession.manually(session.id)
            _uiState.update {
                it.copy(status = SessionStatus.INTERRUPTED)
            }
        }
    }

    /**
     * Formatea milisegundos en representación MM:SS o HH:MM:SS.
     */
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
    }
}
