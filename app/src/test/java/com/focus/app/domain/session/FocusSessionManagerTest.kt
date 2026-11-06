package com.focus.app.domain.session

import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionPhase
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.model.SmartwatchCommand
import com.focus.app.domain.model.SmartwatchDevice
import com.focus.app.domain.model.SmartwatchSettings
import com.focus.app.domain.repository.AppRestrictionRepository
import com.focus.app.domain.repository.FocusSessionRepository
import com.focus.app.domain.repository.SessionForegroundHost
import com.focus.app.domain.repository.SmartwatchGateway
import com.focus.app.domain.repository.SmartwatchSettingsRepository
import com.focus.app.domain.usecase.EndFocusSessionUseCase
import com.focus.app.domain.usecase.StartFocusSessionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusSessionManagerTest {

    private val repository = FakeSessionRepository()
    private val restrictions = FakeRestrictionRepository(setOf(BLOCKED_APP))
    private val settings = FakeSettingsRepository()
    private val watch = FakeSmartwatchGateway()
    private val host = FakeForegroundHost()
    private var clockOverrideMs: Long? = null

    private fun TestScope.createManager(): FocusSessionManager {
        return FocusSessionManager(
            startSession = StartFocusSessionUseCase(repository),
            endSession = EndFocusSessionUseCase(repository),
            sessionRepository = repository,
            restrictionRepository = restrictions,
            settingsRepository = settings,
            smartwatch = watch,
            foregroundHost = host,
            inactivityDetector = InactivityDetector(),
            clock = Clock { clockOverrideMs ?: (testScheduler.currentTime + START_MS) },
            scope = backgroundScope
        )
    }

    private fun TestScope.createInitializedManager(): FocusSessionManager {
        return createManager().also {
            it.initialize()
            runCurrent()
        }
    }

    @Test
    fun `start blocks selected apps and keeps the process alive`() = runTest {
        val manager = createManager()

        val result = manager.start(25, FocusActivityType.WORK)

        assertTrue(result.isSuccess)
        assertTrue(manager.phase.value is SessionPhase.Focusing)
        assertTrue(manager.isRestricted(BLOCKED_APP))
        assertFalse(manager.isRestricted("com.other.app"))
        assertEquals(1, host.startCount)
        assertEquals(1, watch.startedCount)
        assertNotNull(repository.persisted)
    }

    @Test
    fun `start fails while a session is already running`() = runTest {
        val manager = createManager()
        manager.start(25, FocusActivityType.WORK)

        val result = manager.start(45, FocusActivityType.READ)

        assertTrue(result.isFailure)
        assertEquals(1, watch.startedCount)
    }

    @Test
    fun `countdown updates and halfway fires only once`() = runTest {
        val manager = createManager()
        manager.start(10, FocusActivityType.WORK)
        runCurrent()

        advanceTimeBy(6 * MINUTE)
        runCurrent()

        val phase = manager.phase.value as SessionPhase.Focusing
        assertEquals(4 * MINUTE, phase.remainingMillis)
        assertEquals(1, watch.halfwayCount)
    }

    @Test
    fun `watch receives a sparse progress sync instead of one per second`() = runTest {
        val manager = createManager()
        manager.start(10, FocusActivityType.WORK)
        runCurrent()

        advanceTimeBy(3 * MINUTE + 1)
        runCurrent()

        assertEquals(3, watch.progressCount)
    }

    @Test
    fun `expiry unblocks apps and starts the break of the activity`() = runTest {
        val manager = createManager()
        manager.start(25, FocusActivityType.STUDY)
        runCurrent()

        advanceTimeBy(25 * MINUTE + 1)
        runCurrent()

        val phase = manager.phase.value as SessionPhase.Resting
        assertEquals(10, phase.session.breakDurationMinutes)
        assertFalse(manager.isRestricted(BLOCKED_APP))
        assertEquals(1, watch.restCount)
        assertNull(repository.persisted)
    }

    @Test
    fun `break finishing completes the session and notifies the user`() = runTest {
        val manager = createManager()
        manager.start(25, FocusActivityType.WORK)
        runCurrent()

        advanceTimeBy(30 * MINUTE + 2_000)
        runCurrent()

        assertTrue(manager.phase.value is SessionPhase.Completed)
        assertEquals(1, host.completedCount)
        assertEquals(1, watch.stoppedCount)

        manager.acknowledgeCompletion()
        assertEquals(SessionPhase.Idle, manager.phase.value)
    }

    @Test
    fun `expiry skips the break when the rest indicator is disabled`() = runTest {
        settings.current.value = SmartwatchSettings(restIndicatorEnabled = false)
        val manager = createManager()
        manager.start(25, FocusActivityType.WORK)
        runCurrent()

        advanceTimeBy(25 * MINUTE + 1)
        runCurrent()

        assertTrue(manager.phase.value is SessionPhase.Completed)
        assertEquals(0, watch.restCount)
    }

    @Test
    fun `skipBreak goes straight to completed`() = runTest {
        val manager = createManager()
        manager.start(25, FocusActivityType.WORK)
        runCurrent()
        advanceTimeBy(25 * MINUTE + 1)
        runCurrent()

        manager.skipBreak()
        runCurrent()

        assertTrue(manager.phase.value is SessionPhase.Completed)
    }

    @Test
    fun `stop ends the session everywhere`() = runTest {
        val manager = createManager()
        manager.start(25, FocusActivityType.WORK)
        runCurrent()

        manager.stop()
        runCurrent()

        assertEquals(SessionPhase.Idle, manager.phase.value)
        assertFalse(manager.isRestricted(BLOCKED_APP))
        assertEquals(SessionStatus.INTERRUPTED, repository.lastStatus)
        assertNull(repository.persisted)
        assertEquals(1, watch.stoppedCount)
    }

    @Test
    fun `isRestricted is false after the end time even if the timer is delayed`() = runTest {
        val manager = createManager()
        val session = requireNotNull(manager.start(25, FocusActivityType.WORK).getOrNull())
        assertTrue(manager.isRestricted(BLOCKED_APP))

        // Con el teléfono dormido el temporizador no avanza, pero el reloj del sistema sí
        clockOverrideMs = session.endTimestampMs + 1

        assertTrue(manager.phase.value is SessionPhase.Focusing)
        assertFalse(manager.isRestricted(BLOCKED_APP))
    }

    @Test
    fun `an active persisted session is restored after process death`() = runTest {
        repository.persisted = session(endMs = START_MS + 10 * MINUTE)

        val manager = createInitializedManager()

        assertTrue(manager.phase.value is SessionPhase.Focusing)
        assertTrue(manager.isRestricted(BLOCKED_APP))
        assertEquals(1, host.startCount)
        assertEquals(1, watch.restoredCount)
        assertEquals(0, watch.startedCount)
    }

    @Test
    fun `a session that expired during process death is closed`() = runTest {
        repository.persisted = session(endMs = START_MS - 1)

        val manager = createInitializedManager()

        assertEquals(SessionPhase.Idle, manager.phase.value)
        assertNull(repository.persisted)
        assertEquals(SessionStatus.COMPLETED, repository.lastStatus)
        assertEquals(1, watch.stoppedCount)
    }

    @Test
    fun `watch can start a session with the preferred duration`() = runTest {
        val manager = createInitializedManager()

        watch.send(SmartwatchCommand.StartSession(durationMinutes = null, activityType = null))
        runCurrent()

        val phase = manager.phase.value as SessionPhase.Focusing
        assertEquals(PREFERRED_MINUTES, phase.session.durationMinutes)
        assertEquals(FocusActivityType.WORK, phase.session.activityType)
    }

    @Test
    fun `watch can start a session with its own parameters`() = runTest {
        val manager = createInitializedManager()

        watch.send(SmartwatchCommand.StartSession(25, FocusActivityType.READ))
        runCurrent()

        val phase = manager.phase.value as SessionPhase.Focusing
        assertEquals(25, phase.session.durationMinutes)
        assertEquals(FocusActivityType.READ, phase.session.activityType)
    }

    @Test
    fun `watch can stop the session`() = runTest {
        val manager = createInitializedManager()
        manager.start(25, FocusActivityType.WORK)
        runCurrent()

        watch.send(SmartwatchCommand.StopSession)
        runCurrent()

        assertEquals(SessionPhase.Idle, manager.phase.value)
    }

    @Test
    fun `inactivity alert fires at the threshold and honors the setting`() = runTest {
        settings.current.value = SmartwatchSettings(inactivityThresholdMinutes = 30)
        val manager = createManager()
        manager.start(60, FocusActivityType.WORK)
        runCurrent()

        advanceTimeBy(31 * MINUTE)
        runCurrent()

        assertEquals(1, watch.inactivityCount)
        assertTrue(manager.inactivityStatus.value.isAlertActive)
    }

    @Test
    fun `inactivity alert stays silent when disabled`() = runTest {
        settings.current.value = SmartwatchSettings(
            inactivityAlertsEnabled = false,
            inactivityThresholdMinutes = 30
        )
        val manager = createManager()
        manager.start(60, FocusActivityType.WORK)
        runCurrent()

        advanceTimeBy(31 * MINUTE)
        runCurrent()

        assertEquals(0, watch.inactivityCount)
    }

    @Test
    fun `movement from the watch restarts the inactivity count`() = runTest {
        settings.current.value = SmartwatchSettings(inactivityThresholdMinutes = 30)
        val manager = createInitializedManager()
        manager.start(60, FocusActivityType.WORK)
        runCurrent()

        advanceTimeBy(20 * MINUTE)
        watch.send(SmartwatchCommand.MovementDetected)
        runCurrent()
        advanceTimeBy(20 * MINUTE)
        runCurrent()

        assertEquals(0, watch.inactivityCount)
    }

    private fun session(endMs: Long) = FocusSession(
        id = 7L,
        startTimestampMs = endMs - 25 * MINUTE,
        endTimestampMs = endMs,
        durationMinutes = 25,
        selectedPackageNames = setOf(BLOCKED_APP),
        status = SessionStatus.ACTIVE
    )

    private companion object {
        const val MINUTE = 60_000L
        const val START_MS = 1_000_000_000L
        const val BLOCKED_APP = "com.blocked.app"
        const val PREFERRED_MINUTES = 45
    }

    private class FakeSessionRepository : FocusSessionRepository {
        var persisted: FocusSession? = null
        var lastStatus: SessionStatus? = null

        override val activeSession: Flow<FocusSession?> = flowOf(null)

        override suspend fun saveSession(session: FocusSession) {
            persisted = session
        }

        override suspend fun updateSessionStatus(sessionId: Long, newStatus: SessionStatus) {
            lastStatus = newStatus
        }

        override suspend fun loadPersistedSession(): FocusSession? = persisted

        override suspend fun clearActiveSession() {
            persisted = null
        }
    }

    private class FakeRestrictionRepository(
        private val packages: Set<String>
    ) : AppRestrictionRepository {
        override val selectedPackageNames: Flow<Set<String>> = flowOf(packages)
        override val preferredDurationMinutes: Flow<Int> = flowOf(PREFERRED_MINUTES)
        override suspend fun saveSelectedPackages(packageNames: Set<String>) = Unit
        override suspend fun loadSelectedPackages(): Set<String> = packages
        override suspend fun savePreferredDuration(minutes: Int) = Unit
    }

    private class FakeSettingsRepository : SmartwatchSettingsRepository {
        val current = MutableStateFlow(SmartwatchSettings())
        override val settings: Flow<SmartwatchSettings> = current
        override suspend fun setHapticsEnabled(enabled: Boolean) = Unit
        override suspend fun setInactivityAlertsEnabled(enabled: Boolean) = Unit
        override suspend fun setInactivityThresholdMinutes(minutes: Int) = Unit
        override suspend fun setRestIndicatorEnabled(enabled: Boolean) = Unit
    }

    private class FakeForegroundHost : SessionForegroundHost {
        var startCount = 0
        var completedCount = 0

        override fun start() {
            startCount++
        }

        override fun notifyCompleted(session: FocusSession) {
            completedCount++
        }
    }

    private class FakeSmartwatchGateway : SmartwatchGateway {
        private val channel = Channel<SmartwatchCommand>(Channel.UNLIMITED)

        var startedCount = 0
        var restoredCount = 0
        var progressCount = 0
        var halfwayCount = 0
        var inactivityCount = 0
        var restCount = 0
        var stoppedCount = 0

        override val device: StateFlow<SmartwatchDevice> = MutableStateFlow(SmartwatchDevice())
        override val commands: Flow<SmartwatchCommand> = channel.receiveAsFlow()

        fun send(command: SmartwatchCommand) {
            channel.trySend(command)
        }

        override fun refreshConnection() = Unit
        override fun testVibration() = Unit

        override fun onSessionStarted(session: FocusSession) {
            startedCount++
        }

        override fun onSessionRestored(session: FocusSession) {
            restoredCount++
        }

        override fun onProgress(session: FocusSession, remainingMillis: Long) {
            progressCount++
        }

        override fun onHalfwayReached() {
            halfwayCount++
        }

        override fun onInactivityDetected() {
            inactivityCount++
        }

        override fun onRestStarted(session: FocusSession, breakEndTimestampMs: Long) {
            restCount++
        }

        override fun onSessionStopped() {
            stoppedCount++
        }
    }
}
