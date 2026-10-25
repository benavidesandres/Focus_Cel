package com.focus.app.domain.usecase

import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class EndFocusSessionUseCaseTest {

    private lateinit var fakeRepository: FakeEndSessionRepository
    private lateinit var useCase: EndFocusSessionUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeEndSessionRepository()
        useCase = EndFocusSessionUseCase(fakeRepository)
    }

    @Test
    fun `manually ending session updates status to INTERRUPTED and clears session`() = runTest {
        val session = FocusSession(
            id = 100L,
            startTimestampMs = 1000L,
            endTimestampMs = 2000L,
            durationMinutes = 15,
            selectedPackageNames = emptySet(),
            status = SessionStatus.ACTIVE
        )
        fakeRepository.saveSession(session)

        useCase.manually(sessionId = 100L)

        assertEquals(SessionStatus.INTERRUPTED, fakeRepository.lastStatusUpdate)
        assertNull(fakeRepository.persistedSession)
    }

    @Test
    fun `onExpiry with ACTIVE status marks session as COMPLETED and clears session`() = runTest {
        val session = FocusSession(
            id = 200L,
            startTimestampMs = 1000L,
            endTimestampMs = 2000L,
            durationMinutes = 15,
            selectedPackageNames = emptySet(),
            status = SessionStatus.ACTIVE
        )
        fakeRepository.saveSession(session)

        useCase.onExpiry(sessionId = 200L, currentStatus = SessionStatus.ACTIVE)

        assertEquals(SessionStatus.COMPLETED, fakeRepository.lastStatusUpdate)
        assertNull(fakeRepository.persistedSession)
    }

    @Test
    fun `onExpiry with non-active status is a no-op to prevent duplicate triggers`() = runTest {
        useCase.onExpiry(sessionId = 300L, currentStatus = SessionStatus.COMPLETED)
        assertNull(fakeRepository.lastStatusUpdate)
    }
}

private class FakeEndSessionRepository : FocusSessionRepository {
    var persistedSession: FocusSession? = null
    var lastStatusUpdate: SessionStatus? = null
    private val _activeSession = MutableStateFlow<FocusSession?>(null)
    override val activeSession: Flow<FocusSession?> = _activeSession

    override suspend fun saveSession(session: FocusSession) {
        persistedSession = session
        _activeSession.value = session
    }

    override suspend fun updateSessionStatus(sessionId: Long, newStatus: SessionStatus) {
        lastStatusUpdate = newStatus
        persistedSession = persistedSession?.copy(status = newStatus)
        _activeSession.value = persistedSession
    }

    override suspend fun loadPersistedSession(): FocusSession? = persistedSession

    override suspend fun clearActiveSession() {
        persistedSession = null
        _activeSession.value = null
    }
}
