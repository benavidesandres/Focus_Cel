package com.focus.app.domain.repository

import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de sesiones de enfoque.
 * Gestiona la persistencia y recuperación del estado de sesión.
 */
interface FocusSessionRepository {

    /**
     * Flujo observable de la sesión activa actual.
     * Emite null cuando no hay sesión activa o tras completarse.
     */
    val activeSession: Flow<FocusSession?>

    /**
     * Guarda el estado de una nueva sesión activa en almacenamiento local.
     *
     * @param session La sesión a persistir.
     */
    suspend fun saveSession(session: FocusSession)

    /**
     * Actualiza el estado de la sesión activa.
     *
     * @param sessionId ID de la sesión a actualizar.
     * @param newStatus El nuevo estado del ciclo de vida.
     */
    suspend fun updateSessionStatus(sessionId: Long, newStatus: SessionStatus)

    /**
     * Carga la sesión persistida desde almacenamiento local.
     * Útil para reconciliar el estado tras recreación del proceso.
     *
     * @return La sesión almacenada, o null si no existe.
     */
    suspend fun loadPersistedSession(): FocusSession?

    /**
     * Elimina la sesión activa del almacenamiento local.
     * Se llama tras completarse o interrumpirse.
     */
    suspend fun clearActiveSession()
}
