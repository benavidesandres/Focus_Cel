package com.focus.app.domain.usecase

import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartFocusSessionUseCaseTest {

    private lateinit var fakeRepository: FakeFocusSessionRepository
    private lateinit var useCase: StartFocusSessionUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeFocusSessionRepository()
        useCase = StartFocusSessionUseCase(fakeRepository)
    }

    @Test
    fun `valid parameters successfully create and persist session`() = runTest {
        val nowMs = 1_000_000L
        val duration = 25
        val packages = setOf("com.instagram.android", "com.twitter.android")

        val result = useCase(
            durationMinutes = duration,
            selectedPackageNames = packages,
            nowMs = nowMs
        )

        assertTrue(result.isSuccess)
        val session = result.getOrNull()
        assertNotNull(session)
        assertEquals(nowMs, session?.id)
        assertEquals(nowMs, session?.startTimestampMs)
        assertEquals(nowMs + (25 * 60_000L), session?.endTimestampMs)
        assertEquals(SessionStatus.ACTIVE, session?.status)
        assertEquals(packages, session?.selectedPackageNames)
        assertEquals(session, fakeRepository.persistedSession)
    }

    @Test
    fun `zero or negative duration returns failure`() = runTest {
        val zeroResult = useCase(durationMinutes = 0, selectedPackageNames = emptySet())
        assertTrue(zeroResult.isFailure)

        val negativeResult = useCase(durationMinutes = -15, selectedPackageNames = emptySet())
        assertTrue(negativeResult.isFailure)
    }

    @Test
    fun `excessive duration beyond 8 hours returns failure`() = runTest {
        val excessiveResult = useCase(durationMinutes = 481, selectedPackageNames = emptySet())
        assertTrue(excessiveResult.isFailure)
    }

    @Test
    fun `empty package list succeeds as timer-only session`() = runTest {
        val result = useCase(durationMinutes = 45, selectedPackageNames = emptySet())
        assertTrue(result.isSuccess)
        assertEquals(emptySet<String>(), result.getOrNull()?.selectedPackageNames)
    }
}

private class FakeFocusSessionRepository : FocusSessionRepository {
    var persistedSession: FocusSession? = null
    private val _activeSession = MutableStateFlow<FocusSession?>(null)
    override val activeSession: Flow<FocusSession?> = _activeSession

    override suspend fun saveSession(session: FocusSession) {
        persistedSession = session
        _activeSession.value = session
    }

    override suspend fun updateSessionStatus(sessionId: Long, newStatus: SessionStatus) {
        persistedSession = persistedSession?.copy(status = newStatus)
        _activeSession.value = persistedSession
    }

    override suspend fun loadPersistedSession(): FocusSession? = persistedSession

    override suspend fun clearActiveSession() {
        persistedSession = null
        _activeSession.value = null
    }
}
