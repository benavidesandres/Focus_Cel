package com.focus.app.platform.smartwatch

import android.content.Context
import com.focus.app.data.local.preferences.FocusPreferencesDataSource
import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.SmartwatchDevice
import com.focus.app.domain.model.SmartwatchProtocol
import com.focus.app.domain.model.SmartwatchSyncState
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor integral de sincronización entre el Smartphone y el Smartwatch (Wear OS).
 *
 * Administra el canal de comunicación bidireccional usando la Wearable Data Layer API:
 * 1. Envío de inicio, progreso y fin de sesión al reloj.
 * 2. Bloqueo sincronizado de notificaciones (Distraction Shield).
 * 3. Ejecución de patrones de vibración háptica en la muñeca del usuario.
 * 4. Detección y notificación de inactividad física prolongada ("Mucho tiempo sin moverte").
 * 5. Indicador de descanso cuando concluye el tiempo ("Te indica cuándo descansar").
 * 6. Compatibilidad completa con hardware físico Wear OS (Pixel Watch, Galaxy Watch, etc.)
 *    y fallback interactivo a modo complementario cuando no hay un reloj físico emparejado.
 */
@Singleton
class SmartwatchSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataSource: FocusPreferencesDataSource,
    private val hapticHelper: SmartwatchHapticHelper,
    val sedentarismDetector: SedentarismDetector
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
    private val dataClient by lazy { Wearable.getDataClient(context) }
    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(context) }

    private val _connectedWatch = MutableStateFlow(
        SmartwatchDevice(
            name = "Buscando Smartwatch...",
            isConnected = false,
            batteryPercent = 88,
            isSimulated = false
        )
    )
    val connectedWatch: StateFlow<SmartwatchDevice> = _connectedWatch.asStateFlow()

    private val _syncState = MutableStateFlow(SmartwatchSyncState.CONNECTING)
    val syncState: StateFlow<SmartwatchSyncState> = _syncState.asStateFlow()

    init {
        setupDynamicListeners()
        checkConnectedNodes()
        observePreferences()
    }

    /**
     * Registra listeners en tiempo real para reaccionar al instante cuando un reloj
     * físico se empareja, se enciende el Bluetooth o se envían mensajes.
     */
    private fun setupDynamicListeners() {
        try {
            messageClient.addListener { messageEvent ->
                processIncomingMessage(messageEvent.path, messageEvent.data)
            }

            capabilityClient.addListener(
                { _ -> checkConnectedNodes() },
                SmartwatchProtocol.CAPABILITY_WEAR_APP
            )
        } catch (_: Exception) {
            // Ignorado en entornos donde Google Play Services no esté disponible
        }
    }

    /**
     * Escanea y comprueba los relojes Wear OS físicos conectados mediante Google Play Services.
     */
    fun checkConnectedNodes() {
        scope.launch {
            try {
                _syncState.value = SmartwatchSyncState.CONNECTING
                val nodes = nodeClient.connectedNodes.await()

                var wearAppNodeIds = emptySet<String>()
                try {
                    val capabilityInfo = capabilityClient.getCapability(
                        SmartwatchProtocol.CAPABILITY_WEAR_APP,
                        CapabilityClient.FILTER_REACHABLE
                    ).await()
                    wearAppNodeIds = capabilityInfo.nodes.map { it.id }.toSet()
                } catch (_: Exception) {
                    // Si el reloj aún no tiene instalada la app Wear de Focus, se maneja transparentemente
                }

                if (nodes.isNotEmpty()) {
                    // Si hay un nodo que ya tiene la app Wear, se prioriza
                    val primaryNode = nodes.find { wearAppNodeIds.contains(it.id) } ?: nodes.first()

                    _connectedWatch.update {
                        it.copy(
                            id = primaryNode.id,
                            name = primaryNode.displayName.ifBlank { "Smartwatch Wear OS" },
                            isConnected = true,
                            isSimulated = false,
                            isNearby = primaryNode.isNearby,
                            hasWearApp = wearAppNodeIds.contains(primaryNode.id),
                            lastSyncTimestampMs = System.currentTimeMillis()
                        )
                    }
                    _syncState.value = SmartwatchSyncState.CONNECTED

                    // Solicita el estado y nivel de batería del reloj físico
                    requestWatchStatus(primaryNode.id)
                } else {
                    // No hay reloj físico emparejado en este instante; se activa el modo complementario
                    _connectedWatch.update {
                        it.copy(
                            id = "simulated_focus_watch",
                            name = "Focus Watch (Companion)",
                            isConnected = true,
                            isSimulated = true,
                            isNearby = true,
                            hasWearApp = true,
                            lastSyncTimestampMs = System.currentTimeMillis()
                        )
                    }
                    _syncState.value = SmartwatchSyncState.CONNECTED
                }
            } catch (_: Exception) {
                // Fallback seguro al modo complementario activo
                _connectedWatch.update {
                    it.copy(
                        id = "companion_fallback",
                        name = "Focus Watch (Companion)",
                        isConnected = true,
                        isSimulated = true,
                        isNearby = true,
                        hasWearApp = true
                    )
                }
                _syncState.value = SmartwatchSyncState.CONNECTED
            }
        }
    }

    /**
     * Procesa mensajes recibidos desde el Smartwatch (Wear OS).
     */
    fun processIncomingMessage(path: String, data: ByteArray?) {
        scope.launch {
            when (path) {
                SmartwatchProtocol.PATH_BATTERY -> {
                    data?.let { bytes ->
                        try {
                            val json = JSONObject(String(bytes))
                            val battery = json.optInt("battery", -1)
                            if (battery in 0..100) {
                                _connectedWatch.update { it.copy(batteryPercent = battery) }
                            }
                        } catch (_: Exception) {
                            // Ignorado si el formato es inválido
                        }
                    }
                }
                SmartwatchProtocol.PATH_DEVICE_STATUS -> {
                    data?.let { bytes ->
                        try {
                            val json = JSONObject(String(bytes))
                            val model = json.optString("model", "")
                            val battery = json.optInt("battery", -1)
                            _connectedWatch.update { current ->
                                current.copy(
                                    name = if (model.isNotBlank()) model else current.name,
                                    batteryPercent = if (battery in 0..100) battery else current.batteryPercent,
                                    lastSyncTimestampMs = System.currentTimeMillis()
                                )
                            }
                        } catch (_: Exception) {
                            checkConnectedNodes()
                        }
                    } ?: checkConnectedNodes()
                }
                SmartwatchProtocol.PATH_ACCELEROMETER_EVENT -> {
                    // Movimiento físico detectado por los sensores del reloj
                    sedentarismDetector.onMovementDetected()
                }
                SmartwatchProtocol.PATH_INACTIVITY_ALERT -> {
                    notifyInactivityDetected()
                }
                SmartwatchProtocol.PATH_STOP_SESSION -> {
                    notifySessionStopped()
                }
                SmartwatchProtocol.PATH_PING -> {
                    broadcastMessage(SmartwatchProtocol.PATH_PONG, byteArrayOf())
                }
            }
        }
    }

    private fun requestWatchStatus(nodeId: String) {
        scope.launch {
            try {
                messageClient.sendMessage(
                    nodeId,
                    SmartwatchProtocol.PATH_DEVICE_STATUS,
                    byteArrayOf()
                ).await()
            } catch (_: Exception) {
                // Silencioso
            }
        }
    }

    private fun observePreferences() {
        scope.launch {
            val haptic = preferencesDataSource.smartwatchHapticEnabled.first()
            val sedentarism = preferencesDataSource.smartwatchSedentarismEnabled.first()
            val threshold = preferencesDataSource.smartwatchSedentarismThresholdMinutes.first()

            _connectedWatch.update {
                it.copy(
                    hapticVibrationsEnabled = haptic,
                    sedentarismAlertEnabled = sedentarism,
                    sedentarismThresholdMinutes = threshold
                )
            }
        }
    }

    /**
     * Sincroniza el inicio de una sesión con el Smartwatch real y la vista complementaria.
     */
    fun notifySessionStarted(
        activityType: FocusActivityType,
        durationMinutes: Int,
        totalMillis: Long
    ) {
        scope.launch {
            val payload = JSONObject().apply {
                put("action", "START")
                put("activityId", activityType.id)
                put("activityName", activityType.displayName)
                put("durationMinutes", durationMinutes)
                put("totalMillis", totalMillis)
                put("timestamp", System.currentTimeMillis())
            }.toString().toByteArray()

            broadcastMessage(SmartwatchProtocol.PATH_START_SESSION, payload)
            syncActiveSessionData(activityType, durationMinutes, totalMillis)

            // Inicia el detector de inactividad física
            sedentarismDetector.startMonitoring(
                thresholdMinutes = _connectedWatch.value.sedentarismThresholdMinutes
            )

            // Vibración inicial en muñeca
            if (_connectedWatch.value.hapticVibrationsEnabled) {
                hapticHelper.onSessionStart()
                sendHapticToWatch(SmartwatchProtocol.HapticPattern.SESSION_START)
            }
        }
    }

    /**
     * Sincroniza el avance en tiempo real (milisegundos restantes y fracción de progreso).
     */
    fun notifyProgress(
        remainingMillis: Long,
        progressFraction: Float,
        activityType: FocusActivityType
    ) {
        scope.launch {
            val payload = JSONObject().apply {
                put("remainingMillis", remainingMillis)
                put("progress", progressFraction)
                put("activity", activityType.displayName)
            }.toString().toByteArray()

            broadcastMessage(SmartwatchProtocol.PATH_PROGRESS_UPDATE, payload)

            // Evalúa inactividad física
            if (_connectedWatch.value.sedentarismAlertEnabled) {
                val alerted = sedentarismDetector.evaluateStillness()
                if (alerted) {
                    notifyInactivityDetected()
                }
            }
        }
    }

    /**
     * Notifica al reloj que se ha detectado prolongada falta de movimiento ("Mucho tiempo sin moverte").
     */
    fun notifyInactivityDetected() {
        scope.launch {
            val payload = JSONObject().apply {
                put("type", "INACTIVITY")
                put("message", "Llevas mucho tiempo sin moverte. ¡Estira tu cuerpo!")
            }.toString().toByteArray()

            broadcastMessage(SmartwatchProtocol.PATH_INACTIVITY_ALERT, payload)
            if (_connectedWatch.value.hapticVibrationsEnabled) {
                hapticHelper.onInactivityAlert()
                sendHapticToWatch(SmartwatchProtocol.HapticPattern.INACTIVITY_NUDGE)
            }
        }
    }

    /**
     * Notifica al reloj que la sesión terminó y es momento de descansar ("Te indica cuándo descansar").
     */
    fun notifyRestTime(breakDurationMinutes: Int) {
        scope.launch {
            val payload = JSONObject().apply {
                put("type", "REST_BREAK")
                put("breakMinutes", breakDurationMinutes)
                put("message", "¡Gran trabajo! Momento de descansar la mente y los ojos.")
            }.toString().toByteArray()

            broadcastMessage(SmartwatchProtocol.PATH_REST_ALERT, payload)
            if (_connectedWatch.value.hapticVibrationsEnabled) {
                hapticHelper.onRestTimeAlert()
                sendHapticToWatch(SmartwatchProtocol.HapticPattern.REST_TIME)
            }
            sedentarismDetector.stopMonitoring()
        }
    }

    /**
     * Sincroniza la detención o interrupción de la sesión.
     */
    fun notifySessionStopped() {
        scope.launch {
            val payload = JSONObject().apply {
                put("action", "STOP")
            }.toString().toByteArray()

            broadcastMessage(SmartwatchProtocol.PATH_STOP_SESSION, payload)
            sedentarismDetector.stopMonitoring()
        }
    }

    /**
     * Envía una instrucción de patrón háptico directo al Smartwatch físico.
     */
    private suspend fun sendHapticToWatch(pattern: SmartwatchProtocol.HapticPattern) {
        try {
            val payload = JSONObject().apply {
                put("pattern", pattern.name)
                put("description", pattern.description)
            }.toString().toByteArray()
            broadcastMessage(SmartwatchProtocol.PATH_HAPTIC_TRIGGER, payload)
        } catch (_: Exception) {
            // Silencioso
        }
    }

    /**
     * Envía un mensaje a todos los nodos Wear OS conectados.
     */
    private suspend fun broadcastMessage(path: String, data: ByteArray) {
        try {
            val nodes = nodeClient.connectedNodes.await()
            for (node in nodes) {
                messageClient.sendMessage(node.id, path, data).await()
            }
        } catch (_: Exception) {
            // Silencioso para garantizar que la app nunca falle si el reloj pierde señal temporalmente
        }
    }

    /**
     * Sincroniza el mapa de datos continuo a través de DataClient.
     */
    private suspend fun syncActiveSessionData(
        activity: FocusActivityType,
        durationMinutes: Int,
        totalMillis: Long
    ) {
        try {
            val putDataReq = PutDataMapRequest.create(SmartwatchProtocol.DATA_PATH_ACTIVE_SESSION).apply {
                dataMap.putString("activity", activity.displayName)
                dataMap.putInt("durationMinutes", durationMinutes)
                dataMap.putLong("startTimeMs", System.currentTimeMillis())
                dataMap.putLong("totalMillis", totalMillis)
            }.asPutDataRequest().setUrgent()

            dataClient.putDataItem(putDataReq).await()
        } catch (_: Exception) {
            // Ignorado si no hay receptor activo en ese momento
        }
    }

    /**
     * Registra un evento de movimiento recibido del reloj físico o de prueba.
     */
    fun simulateMovement() {
        sedentarismDetector.onMovementDetected()
    }

    /**
     * Prueba la vibración háptica coordinada en el reloj físico y en el teléfono.
     */
    fun testHapticVibration() {
        hapticHelper.onSessionStart()
        scope.launch {
            sendHapticToWatch(SmartwatchProtocol.HapticPattern.SESSION_START)
        }
    }
}
