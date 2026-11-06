package com.focus.app.platform.accessibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.core.util.formatCountdown
import com.focus.app.domain.model.SessionPhase
import com.focus.app.domain.session.FocusSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel para la pantalla de restricción.
 * Refleja el tiempo restante publicado por el gestor de sesiones.
 */
@HiltViewModel
class RestrictionViewModel @Inject constructor(
    private val sessionManager: FocusSessionManager
) : ViewModel() {

    val uiState: StateFlow<RestrictionUiState> = sessionManager.phase
        .map(::toUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = toUiState(sessionManager.phase.value)
        )

    /** Termina la sesión activa manualmente desde la pantalla de restricción. */
    fun onEndSession() {
        sessionManager.stop()
    }

    private fun toUiState(phase: SessionPhase): RestrictionUiState {
        return if (phase is SessionPhase.Focusing) {
            RestrictionUiState(
                isSessionActive = true,
                timeRemainingLabel = formatCountdown(phase.remainingMillis)
            )
        } else {
            RestrictionUiState(isSessionActive = false)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/**
 * Estado de la UI de la pantalla de restricción.
 *
 * @param isSessionActive Si la sesión sigue activa; si no, la pantalla debe cerrarse.
 * @param timeRemainingLabel Tiempo restante formateado.
 */
data class RestrictionUiState(
    val isSessionActive: Boolean = true,
    val timeRemainingLabel: String = ""
)
