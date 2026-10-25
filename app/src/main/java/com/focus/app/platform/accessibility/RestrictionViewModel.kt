package com.focus.app.platform.accessibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.domain.usecase.EndFocusSessionUseCase
import com.focus.app.domain.usecase.ObserveFocusSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de restricción.
 * Observa la sesión activa y calcula el tiempo restante cada segundo.
 */
@HiltViewModel
class RestrictionViewModel @Inject constructor(
    private val observeSession: ObserveFocusSessionUseCase,
    private val endSession: EndFocusSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestrictionUiState())
    val uiState: StateFlow<RestrictionUiState> = _uiState.asStateFlow()

    private var blockedPackage: String = ""
    private var sessionId: Long = -1L
    private var tickJob: Job? = null

    /**
     * Inicializa el ViewModel con los datos del paquete bloqueado y la sesión.
     * Comienza a actualizar el tiempo restante cada segundo.
     */
    fun initialize(blockedPackage: String, sessionId: Long) {
        this.blockedPackage = blockedPackage
        this.sessionId = sessionId
        startTicking()
    }

    /**
     * Inicia el bucle de actualización del tiempo restante (1 segundo de intervalo).
     * Usa el timestamp de sesión almacenado, no un contador simple, para ser preciso.
     */
    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                updateRemainingTime()
                delay(1_000L)
            }
        }
    }

    /**
     * Calcula el tiempo restante basándose en el timestamp de fin de sesión almacenado.
     */
    private suspend fun updateRemainingTime() {
        val session = observeSession().let { flow ->
            var result: com.focus.app.domain.model.FocusSession? = null
            flow.collect { result = it; return@collect }
            result
        } ?: return

        val nowMs = System.currentTimeMillis()
        val remainingMs = session.remainingMillis(nowMs)

        _uiState.value = RestrictionUiState(
            timeRemainingLabel = formatRemainingTime(remainingMs)
        )
    }

    /**
     * Formatea los milisegundos restantes como MM:SS.
     */
    private fun formatRemainingTime(remainingMs: Long): String {
        val totalSeconds = remainingMs / 1_000L
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    /**
     * Termina la sesión activa manualmente desde la pantalla de restricción.
     */
    fun endSession() {
        viewModelScope.launch {
            if (sessionId != -1L) {
                endSession.manually(sessionId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}

/**
 * Estado de la UI de la pantalla de restricción.
 *
 * @param timeRemainingLabel Tiempo restante formateado como MM:SS.
 */
data class RestrictionUiState(
    val timeRemainingLabel: String = ""
)
