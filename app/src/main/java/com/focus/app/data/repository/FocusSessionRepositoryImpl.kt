package com.focus.app.data.repository

import com.focus.app.data.local.preferences.FocusPreferencesDataSource
import com.focus.app.data.mapper.SessionMapper
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de sesiones de enfoque.
 * Combina un StateFlow en memoria para actualizaciones reactivas
 * con DataStore para persistencia entre procesos.
 */
@Singleton
class FocusSessionRepositoryImpl @Inject constructor(
    private val dataSource: FocusPreferencesDataSource
) : FocusSessionRepository {

    // Estado en memoria para actualizaciones reactivas inmediatas
    private val _activeSession = MutableStateFlow<FocusSession?>(null)

    override val activeSession: Flow<FocusSession?> = _activeSession.asStateFlow()

    override suspend fun saveSession(session: FocusSession) {
        // Persiste en DataStore para sobrevivir a la recreación del proceso
        dataSource.saveActiveSession(
            id = session.id,
            startMs = session.startTimestampMs,
            endMs = session.endTimestampMs,
            durationMinutes = session.durationMinutes,
            packageNames = session.selectedPackageNames,
            status = SessionMapper.toStatusString(session.status)
        )
        // Actualiza el flujo en memoria para reactividad inmediata
        _activeSession.value = session
    }

    override suspend fun updateSessionStatus(sessionId: Long, newStatus: SessionStatus) {
        // Actualiza el flujo en memoria si la sesión activa coincide con el ID
        val current = _activeSession.value
        if (current?.id == sessionId) {
            _activeSession.value = current.copy(status = newStatus)
        }
        // Persiste el nuevo estado en DataStore
        dataSource.updateSessionStatus(SessionMapper.toStatusString(newStatus))
    }

    override suspend fun loadPersistedSession(): FocusSession? {
        val snapshot = dataSource.loadActiveSession() ?: return null
        val session = SessionMapper.toDomain(snapshot)

        // Sincroniza el estado en memoria al cargar desde DataStore
        if (session != null) {
            _activeSession.value = session
        }

        return session
    }

    override suspend fun clearActiveSession() {
        // Limpia el flujo en memoria primero para actualizar la UI inmediatamente
        _activeSession.value = null
        // Luego limpia la persistencia
        dataSource.clearActiveSession()
    }
}
