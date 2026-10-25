package com.focus.app.domain.usecase

import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para observar y reconciliar la sesión de enfoque activa.
 */
class ObserveFocusSessionUseCase @Inject constructor(
    private val sessionRepository: FocusSessionRepository
) {
    /**
     * Flujo observable de la sesión activa.
     * Emite null cuando no hay sesión activa.
     */
    operator fun invoke(): Flow<FocusSession?> = sessionRepository.activeSession

    /**
     * Reconcilia el estado de la sesión persistida con el tiempo actual.
     * Útil al iniciar la app o tras recreación del proceso.
     *
     * Si la sesión almacenada ya expiró, la finaliza y devuelve null.
     * Si sigue activa, la devuelve para restaurar la UI.
     *
     * @param nowMs Timestamp actual en milisegundos.
     * @param endOnExpiry Callback invocado si la sesión ya expiró.
     * @return La sesión activa si está vigente, o null.
     */
    suspend fun reconcileOnStartup(
        nowMs: Long = System.currentTimeMillis(),
        endOnExpiry: suspend (sessionId: Long) -> Unit
    ): FocusSession? {
        val persisted = sessionRepository.loadPersistedSession() ?: return null

        return if (persisted.isExpired(nowMs)) {
            // La sesión expiró mientras la app estaba cerrada o en segundo plano
            endOnExpiry(persisted.id)
            null
        } else {
            persisted
        }
    }
}
