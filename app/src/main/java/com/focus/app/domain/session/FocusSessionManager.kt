package com.focus.app.domain.session

import com.focus.app.core.di.ApplicationScope
import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionPhase
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.model.SmartwatchCommand
import com.focus.app.domain.repository.AppRestrictionRepository
import com.focus.app.domain.repository.FocusSessionRepository
import com.focus.app.domain.repository.SessionForegroundHost
import com.focus.app.domain.repository.SmartwatchGateway
import com.focus.app.domain.repository.SmartwatchSettingsRepository
import com.focus.app.domain.usecase.EndFocusSessionUseCase
import com.focus.app.domain.usecase.StartFocusSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Núcleo del ciclo de vida de una sesión de enfoque.
 *
 * Vive tanto como el proceso, de modo que la sesión sigue funcionando aunque ninguna
 * pantalla esté abierta. Todas las entradas (UI, pantalla de bloqueo y reloj) pasan por
 * aquí, por lo que cualquier cambio se refleja igual en el bloqueo, la notificación y el reloj.
 *
 * Las transiciones se serializan con un Mutex. El temporizador usa timestamps absolutos,
 * así que sobrevive a la muerte del proceso y a las pausas del sistema.
 */
@Singleton
class FocusSessionManager @Inject constructor(
    private val startSession: StartFocusSessionUseCase,
    private val endSession: EndFocusSessionUseCase,
    private val sessionRepository: FocusSessionRepository,
    private val restrictionRepository: AppRestrictionRepository,
    private val settingsRepository: SmartwatchSettingsRepository,
    private val smartwatch: SmartwatchGateway,
    private val foregroundHost: SessionForegroundHost,
    private val inactivityDetector: InactivityDetector,
    private val clock: Clock,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val _phase = MutableStateFlow<SessionPhase>(SessionPhase.Idle)

    /** Fase actual de la sesión. */
    val phase: StateFlow<SessionPhase> = _phase.asStateFlow()

    /** Estado del detector de inactividad de la sesión en curso. */
    val inactivityStatus: StateFlow<InactivityStatus> = inactivityDetector.status

    private val mutex = Mutex()
    private val isInitialized = AtomicBoolean(false)
    private var timerJob: Job? = null
    private var isHalfwayNotified = false

    /**
     * Restaura la sesión persistida y empieza a atender al reloj.
     * Se llama al crear el proceso; las llamadas siguientes no tienen efecto.
     */
    fun initialize() {
        if (!isInitialized.compareAndSet(false, true)) return
        scope.launch { restorePersistedSession() }
        scope.launch { smartwatch.commands.collect(::handleSmartwatchCommand) }
    }

    /**
     * Inicia una sesión bloqueando las apps seleccionadas actualmente.
     *
     * @param durationMinutes Duración de la sesión en minutos.
     * @param activityType Actividad elegida por el usuario.
     * @return La sesión creada, o un error si ya hay otra en curso o los datos no son válidos.
     */
    suspend fun start(
        durationMinutes: Int,
        activityType: FocusActivityType
    ): Result<FocusSession> = mutex.withLock {
        if (_phase.value is SessionPhase.Focusing) {
            return@withLock Result.failure(
                IllegalStateException("A focus session is already running.")
            )
        }

        timerJob?.cancel()
        startSession(
            durationMinutes = durationMinutes,
            selectedPackageNames = restrictionRepository.loadSelectedPackages(),
            activityType = activityType,
            nowMs = clock.nowMs()
        ).onSuccess { session ->
            enterFocusing(session)
            smartwatch.onSessionStarted(session)
        }
    }

    /** Termina la sesión o el descanso en curso antes de tiempo. */
    fun stop() {
        scope.launch {
            mutex.withLock {
                timerJob?.cancel()
                val phase = _phase.value
                if (phase is SessionPhase.Focusing) endSession.manually(phase.session.id)
                if (phase is SessionPhase.Focusing || phase is SessionPhase.Resting) {
                    smartwatch.onSessionStopped()
                }
                inactivityDetector.stopMonitoring()
                _phase.value = SessionPhase.Idle
            }
        }
    }

    /** Omite el descanso guiado y pasa directamente al resumen. */
    fun skipBreak() {
        scope.launch {
            mutex.withLock {
                val phase = _phase.value as? SessionPhase.Resting ?: return@withLock
                timerJob?.cancel()
                complete(phase.session)
            }
        }
    }

    /** El usuario cerró el resumen de la sesión completada. */
    fun acknowledgeCompletion() {
        _phase.update { if (it is SessionPhase.Completed) SessionPhase.Idle else it }
    }

    /** El usuario confirmó en el teléfono que se movió. */
    fun acknowledgeMovement() {
        inactivityDetector.onMovementDetected(clock.nowMs())
    }

    /**
     * Indica si un paquete debe bloquearse en este instante.
     * Comprueba el reloj para no bloquear si el temporizador se retrasó con el teléfono dormido.
     *
     * @param packageName Paquete que pasó al primer plano.
     */
    fun isRestricted(packageName: String): Boolean {
        val session = (_phase.value as? SessionPhase.Focusing)?.session ?: return false
        return packageName in session.selectedPackageNames && !session.isExpired(clock.nowMs())
    }

    private suspend fun restorePersistedSession() = mutex.withLock {
        if (_phase.value != SessionPhase.Idle) return@withLock
        val session = sessionRepository.loadPersistedSession() ?: return@withLock

        when {
            session.status != SessionStatus.ACTIVE -> sessionRepository.clearActiveSession()
            session.isExpired(clock.nowMs()) -> {
                // La sesión expiró mientras el proceso no existía
                endSession.onExpiry(session.id, session.status)
                smartwatch.onSessionStopped()
            }
            else -> {
                enterFocusing(session)
                smartwatch.onSessionRestored(session)
            }
        }
    }

    private suspend fun handleSmartwatchCommand(command: SmartwatchCommand) {
        when (command) {
            is SmartwatchCommand.StartSession -> start(
                durationMinutes = command.durationMinutes
                    ?: restrictionRepository.preferredDurationMinutes.first(),
                activityType = command.activityType ?: FocusActivityType.WORK
            )
            SmartwatchCommand.StopSession -> stop()
            SmartwatchCommand.MovementDetected,
            SmartwatchCommand.DismissInactivityAlert -> acknowledgeMovement()
        }
    }

    private suspend fun enterFocusing(session: FocusSession) {
        val nowMs = clock.nowMs()
        val remainingMs = session.remainingMillis(nowMs)
        isHalfwayNotified = isPastHalfway(session, remainingMs)

        inactivityDetector.startMonitoring(
            thresholdMinutes = settingsRepository.settings.first().inactivityThresholdMinutes,
            nowMs = nowMs
        )
        _phase.value = SessionPhase.Focusing(session, remainingMs)
        foregroundHost.start()
        timerJob = scope.launch { runFocusTimer(session) }
    }

    private suspend fun runFocusTimer(session: FocusSession) {
        var lastSyncMs = clock.nowMs()

        while (true) {
            val nowMs = clock.nowMs()
            if (session.isExpired(nowMs)) {
                onFocusExpired(session)
                return
            }

            val remainingMs = session.remainingMillis(nowMs)
            // Solo actualiza si la sesión sigue en curso, por si se detuvo durante este tick
            _phase.update { phase ->
                if (phase is SessionPhase.Focusing && phase.session.id == session.id) {
                    phase.copy(remainingMillis = remainingMs)
                } else {
                    phase
                }
            }

            if (!isHalfwayNotified && isPastHalfway(session, remainingMs)) {
                isHalfwayNotified = true
                smartwatch.onHalfwayReached()
            }

            if (nowMs - lastSyncMs >= WATCH_SYNC_INTERVAL_MS) {
                lastSyncMs = nowMs
                smartwatch.onProgress(session, remainingMs)
                evaluateInactivity(nowMs)
            }

            delay(TICK_INTERVAL_MS)
        }
    }

    private suspend fun evaluateInactivity(nowMs: Long) {
        val isEnabled = settingsRepository.settings.first().inactivityAlertsEnabled
        if (isEnabled && inactivityDetector.evaluate(nowMs)) {
            smartwatch.onInactivityDetected()
        }
    }

    private suspend fun onFocusExpired(session: FocusSession) = mutex.withLock {
        val phase = _phase.value as? SessionPhase.Focusing ?: return@withLock
        if (phase.session.id != session.id) return@withLock

        endSession.onExpiry(session.id, session.status)
        inactivityDetector.stopMonitoring()

        if (settingsRepository.settings.first().restIndicatorEnabled) {
            enterRest(session)
        } else {
            complete(session)
        }
    }

    private fun enterRest(session: FocusSession) {
        val breakMillis = session.breakDurationMinutes * MILLIS_PER_MINUTE
        val breakEndMs = clock.nowMs() + breakMillis

        _phase.value = SessionPhase.Resting(session, breakEndMs, breakMillis)
        smartwatch.onRestStarted(session, breakEndMs)
        timerJob = scope.launch { runBreakTimer(session, breakEndMs) }
    }

    private suspend fun runBreakTimer(session: FocusSession, breakEndMs: Long) {
        while (true) {
            val remainingMs = breakEndMs - clock.nowMs()
            if (remainingMs <= 0L) {
                onBreakFinished(session)
                return
            }

            _phase.update { phase ->
                if (phase is SessionPhase.Resting && phase.session.id == session.id) {
                    phase.copy(remainingMillis = remainingMs)
                } else {
                    phase
                }
            }
            delay(TICK_INTERVAL_MS)
        }
    }

    private suspend fun onBreakFinished(session: FocusSession) = mutex.withLock {
        val phase = _phase.value as? SessionPhase.Resting ?: return@withLock
        if (phase.session.id == session.id) complete(session)
    }

    private fun complete(session: FocusSession) {
        _phase.value = SessionPhase.Completed(session)
        smartwatch.onSessionStopped()
        foregroundHost.notifyCompleted(session)
    }

    private fun isPastHalfway(session: FocusSession, remainingMs: Long): Boolean {
        return remainingMs * 2 <= session.endTimestampMs - session.startTimestampMs
    }

    private companion object {
        const val TICK_INTERVAL_MS = 1_000L

        // Enviar cada segundo por Bluetooth agotaría la batería; el reloj cuenta por su cuenta
        const val WATCH_SYNC_INTERVAL_MS = 60_000L
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
