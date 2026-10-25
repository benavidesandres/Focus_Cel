package com.focus.app.domain.usecase

import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.repository.FocusSessionRepository
import javax.inject.Inject

/**
 * Caso de uso para finalizar una sesión de enfoque.
 * Actualiza el estado y delega la limpieza al repositorio.
 */
class EndFocusSessionUseCase @Inject constructor(
    private val sessionRepository: FocusSessionRepository
) {
    /**
     * Finaliza la sesión activa de forma manual (el usuario presionó "Terminar sesión").
     * Marca la sesión como interrumpida.
     *
     * @param sessionId ID de la sesión activa.
     */
    suspend fun manually(sessionId: Long) {
        sessionRepository.updateSessionStatus(sessionId, SessionStatus.INTERRUPTED)
        sessionRepository.clearActiveSession()
    }

    /**
     * Marca la sesión activa como completada (el temporizador llegó a cero).
     * Evita duplicados: si ya está completada o interrumpida, no hace nada.
     *
     * @param sessionId ID de la sesión activa.
     * @param currentStatus Estado actual de la sesión para prevenir duplicados.
     */
    suspend fun onExpiry(sessionId: Long, currentStatus: SessionStatus) {
        // Prevención de eventos de expiración duplicados
        if (currentStatus != SessionStatus.ACTIVE) return

        sessionRepository.updateSessionStatus(sessionId, SessionStatus.COMPLETED)
        sessionRepository.clearActiveSession()
    }
}
