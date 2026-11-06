package com.focus.app.platform.smartwatch

import android.content.Context
import android.util.Log
import com.focus.app.core.di.ApplicationScope
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SmartwatchCommand
import com.focus.app.domain.model.SmartwatchConnectionState
import com.focus.app.domain.model.SmartwatchDevice
import com.focus.app.domain.model.SmartwatchProtocol
import com.focus.app.domain.model.SmartwatchProtocol.HapticPattern
import com.focus.app.domain.model.SmartwatchSettings
import com.focus.app.domain.repository.SmartwatchGateway
import com.focus.app.domain.repository.SmartwatchSettingsRepository
import com.focus.app.domain.session.Clock
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de la comunicación con el reloj sobre la Wearable Data Layer.
 *
 * - El estado de la sesión se publica como DataItem con timestamps absolutos: la Data Layer lo
 *   entrega aunque el reloj esté desconectado en ese momento, y el reloj cuenta por su cuenta.
 * - Los eventos puntuales (inicio, alertas, vibraciones) viajan como mensajes.
 * - Los envíos se procesan en orden para que un "detener" nunca adelante a un "iniciar".
 *
 * Refleja siempre el estado real: si no hay reloj, lo informa como desconectado.
 */
@Singleton
class SmartwatchSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    settingsRepository: SmartwatchSettingsRepository,
    private val hapticHelper: SmartwatchHapticHelper,
    private val clock: Clock,
    @ApplicationScope private val scope: CoroutineScope
) : SmartwatchGateway {

    private val nodeClient by lazy { Wearable.getNodeClient(context) }
    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val dataClient by lazy { Wearable.getDataClient(context) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(context) }

    private val _device = MutableStateFlow(SmartwatchDevice())
    override val device: StateFlow<SmartwatchDevice> = _device.asStateFlow()

    private val commandChannel = Channel<SmartwatchCommand>(Channel.BUFFERED)
    override val commands: Flow<SmartwatchCommand> = commandChannel.receiveAsFlow()

    private val settings: StateFlow<SmartwatchSettings> = settingsRepository.settings
        .stateIn(scope, SharingStarted.Eagerly, SmartwatchSettings())

    // Cola de envíos: un único consumidor garantiza el orden
    private val outbox = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (task in outbox) {
                runWearableCall(task)
            }
        }
        scope.launch {
            settingsRepository.settings.collect { current ->
                enqueue { publishPreferences(current) }
            }
        }
        refreshConnection()
    }

    override fun refreshConnection() {
        scope.launch {
            _device.update { current ->
                if (current.isConnected) {
                    current
                } else {
                    current.copy(connectionState = SmartwatchConnectionState.SEARCHING)
                }
            }

            val detected = runWearableCall { detectDevice() } ?: SmartwatchDevice(
                connectionState = SmartwatchConnectionState.DISCONNECTED
            )

            _device.update { current ->
                // Conserva la batería ya informada si sigue siendo el mismo reloj
                val battery = current.batteryPercent.takeIf { current.id == detected.id }
                detected.copy(batteryPercent = battery)
            }

            detected.id?.takeIf { detected.isConnected }?.let(::requestWatchStatus)
        }
    }

    private suspend fun detectDevice(): SmartwatchDevice {
        val nodes = nodeClient.connectedNodes.await()
        val wearAppNodeIds = reachableWearAppNodeIds()
        val primaryNode = nodes.firstOrNull { it.id in wearAppNodeIds } ?: nodes.firstOrNull()
            ?: return SmartwatchDevice(connectionState = SmartwatchConnectionState.DISCONNECTED)

        return SmartwatchDevice(
            id = primaryNode.id,
            name = primaryNode.displayName,
            connectionState = if (primaryNode.id in wearAppNodeIds) {
                SmartwatchConnectionState.CONNECTED
            } else {
                SmartwatchConnectionState.MISSING_WEAR_APP
            },
            isNearby = primaryNode.isNearby
        )
    }

    private fun requestWatchStatus(nodeId: String) {
        enqueue {
            messageClient.sendMessage(nodeId, SmartwatchProtocol.PATH_DEVICE_STATUS, EMPTY_PAYLOAD)
                .await()
        }
    }

    /**
     * Procesa un mensaje recibido del reloj.
     *
     * @param sourceNodeId Nodo que envió el mensaje.
     * @param path Ruta del mensaje.
     * @param data Carga útil.
     */
    fun onMessageReceived(sourceNodeId: String, path: String, data: ByteArray) {
        when (val message = SmartwatchMessageParser.parse(path, data)) {
            is SmartwatchMessage.Command -> commandChannel.trySend(message.command)
            is SmartwatchMessage.Status -> _device.update { current ->
                current.copy(
                    name = message.model ?: current.name,
                    batteryPercent = message.batteryPercent ?: current.batteryPercent
                )
            }
            SmartwatchMessage.Ping -> enqueue {
                messageClient.sendMessage(sourceNodeId, SmartwatchProtocol.PATH_PONG, EMPTY_PAYLOAD)
                    .await()
            }
            null -> Log.d(TAG, "Ignored message on path $path")
        }
    }

    override fun testVibration() {
        playHaptic(HapticPattern.SESSION_START, ignoreSettings = true)
    }

    override fun onSessionStarted(session: FocusSession) {
        val payload = JSONObject()
            .put("action", "START")
            .put("activityId", session.activityType.id)
            .put("activityName", session.activityType.displayName)
            .put("durationMinutes", session.durationMinutes)
            .put("endTimestampMs", session.endTimestampMs)

        enqueue {
            publishSessionState(STATE_FOCUSING, session, session.endTimestampMs)
            sendToWatch(SmartwatchProtocol.PATH_START_SESSION, payload)
        }
        playHaptic(HapticPattern.SESSION_START)
    }

    override fun onSessionRestored(session: FocusSession) {
        enqueue { publishSessionState(STATE_FOCUSING, session, session.endTimestampMs) }
    }

    override fun onProgress(session: FocusSession, remainingMillis: Long) {
        val payload = JSONObject()
            .put("remainingMillis", remainingMillis)
            .put("activityId", session.activityType.id)

        enqueue { sendToWatch(SmartwatchProtocol.PATH_PROGRESS_UPDATE, payload) }
    }

    override fun onHalfwayReached() {
        playHaptic(HapticPattern.HALFWAY_MILESTONE)
    }

    override fun onInactivityDetected() {
        val payload = JSONObject().put("type", "INACTIVITY")

        enqueue { sendToWatch(SmartwatchProtocol.PATH_INACTIVITY_ALERT, payload) }
        playHaptic(HapticPattern.INACTIVITY_NUDGE)
    }

    override fun onRestStarted(session: FocusSession, breakEndTimestampMs: Long) {
        val payload = JSONObject()
            .put("type", "REST_BREAK")
            .put("breakMinutes", session.breakDurationMinutes)
            .put("breakEndTimestampMs", breakEndTimestampMs)

        enqueue {
            publishSessionState(STATE_RESTING, session, breakEndTimestampMs)
            sendToWatch(SmartwatchProtocol.PATH_REST_ALERT, payload)
        }
        playHaptic(HapticPattern.REST_TIME)
    }

    override fun onSessionStopped() {
        val payload = JSONObject().put("action", "STOP")

        enqueue {
            publishSessionState(STATE_IDLE, session = null, endTimestampMs = 0L)
            sendToWatch(SmartwatchProtocol.PATH_STOP_SESSION, payload)
        }
    }

    private fun playHaptic(pattern: HapticPattern, ignoreSettings: Boolean = false) {
        if (!ignoreSettings && !settings.value.hapticsEnabled) return

        hapticHelper.play(pattern)
        val payload = JSONObject().put("pattern", pattern.name)
        enqueue { sendToWatch(SmartwatchProtocol.PATH_HAPTIC_TRIGGER, payload) }
    }

    private suspend fun sendToWatch(path: String, payload: JSONObject) {
        val bytes = payload.toString().toByteArray(Charsets.UTF_8)
        reachableWearAppNodeIds().forEach { nodeId ->
            messageClient.sendMessage(nodeId, path, bytes).await()
        }
    }

    private suspend fun reachableWearAppNodeIds(): Set<String> {
        return capabilityClient
            .getCapability(SmartwatchProtocol.CAPABILITY_WEAR_APP, CapabilityClient.FILTER_REACHABLE)
            .await()
            .nodes
            .mapTo(mutableSetOf()) { it.id }
    }

    private suspend fun publishSessionState(
        state: String,
        session: FocusSession?,
        endTimestampMs: Long
    ) {
        val request = PutDataMapRequest.create(SmartwatchProtocol.DATA_PATH_ACTIVE_SESSION).apply {
            dataMap.putString("state", state)
            dataMap.putString("activityId", session?.activityType?.id.orEmpty())
            dataMap.putString("activityName", session?.activityType?.displayName.orEmpty())
            dataMap.putInt("durationMinutes", session?.durationMinutes ?: 0)
            dataMap.putLong("endTimestampMs", endTimestampMs)
            // Garantiza que el cambio se notifique aunque el resto de campos coincida
            dataMap.putLong("updatedAtMs", clock.nowMs())
        }.asPutDataRequest().setUrgent()

        dataClient.putDataItem(request).await()
    }

    private suspend fun publishPreferences(current: SmartwatchSettings) {
        val request = PutDataMapRequest.create(SmartwatchProtocol.DATA_PATH_PREFERENCES).apply {
            dataMap.putBoolean("hapticsEnabled", current.hapticsEnabled)
            dataMap.putBoolean("inactivityAlertsEnabled", current.inactivityAlertsEnabled)
            dataMap.putInt("inactivityThresholdMinutes", current.inactivityThresholdMinutes)
            dataMap.putBoolean("restIndicatorEnabled", current.restIndicatorEnabled)
        }.asPutDataRequest()

        dataClient.putDataItem(request).await()
    }

    private fun enqueue(task: suspend () -> Unit) {
        outbox.trySend(task)
    }

    /**
     * Ejecuta una llamada a la Data Layer. Falla de forma habitual si el reloj pierde señal o
     * el dispositivo no tiene la API de Wear OS, así que el error solo se registra.
     *
     * @return El resultado de la llamada, o null si falló.
     */
    private suspend fun <T> runWearableCall(call: suspend () -> T): T? {
        return try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Wearable call failed: ${e.message}")
            null
        }
    }

    private companion object {
        const val TAG = "SmartwatchSync"
        const val STATE_FOCUSING = "FOCUSING"
        const val STATE_RESTING = "RESTING"
        const val STATE_IDLE = "IDLE"
        val EMPTY_PAYLOAD = ByteArray(0)
    }
}
