package com.focus.app.data.repository

import com.focus.app.data.local.preferences.FocusPreferencesDataSource
import com.focus.app.data.mapper.SessionMapper
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de sesiones de enfoque.
 * DataStore es la única fuente de verdad, por lo que el estado sobrevive a la muerte del proceso.
 */
@Singleton
class FocusSessionRepositoryImpl @Inject constructor(
    private val dataSource: FocusPreferencesDataSource
) : FocusSessionRepository {

    override val activeSession: Flow<FocusSession?> = dataSource.activeSession
        .map { snapshot -> snapshot?.let(SessionMapper::toDomain) }

    override suspend fun saveSession(session: FocusSession) {
        dataSource.saveActiveSession(
            id = session.id,
            startMs = session.startTimestampMs,
            endMs = session.endTimestampMs,
            durationMinutes = session.durationMinutes,
            packageNames = session.selectedPackageNames,
            status = SessionMapper.toStatusString(session.status),
            activityType = session.activityType.id
        )
    }

    override suspend fun updateSessionStatus(sessionId: Long, newStatus: SessionStatus) {
        if (dataSource.loadActiveSession()?.id == sessionId) {
            dataSource.updateSessionStatus(SessionMapper.toStatusString(newStatus))
        }
    }

    override suspend fun loadPersistedSession(): FocusSession? {
        return dataSource.loadActiveSession()?.let(SessionMapper::toDomain)
    }

    override suspend fun clearActiveSession() {
        dataSource.clearActiveSession()
    }
}
