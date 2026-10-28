package com.focus.app.platform.smartwatch

import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Servicio de escucha en segundo plano para eventos provenientes del Smartwatch (Wear OS).
 *
 * Recibe eventos cuando el usuario interactúa directamente con el reloj,
 * cuando los sensores de movimiento del reloj detectan patrones,
 * o cuando el estado de conexión del reloj físico cambia.
 */
@AndroidEntryPoint
class FocusWearableListenerService : WearableListenerService() {

    @Inject
    lateinit var syncManager: SmartwatchSyncManager

    override fun onMessageReceived(messageEvent: MessageEvent) {
        syncManager.processIncomingMessage(messageEvent.path, messageEvent.data)
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        syncManager.checkConnectedNodes()
    }

    override fun onPeerConnected(peer: Node) {
        syncManager.checkConnectedNodes()
    }

    override fun onPeerDisconnected(peer: Node) {
        syncManager.checkConnectedNodes()
    }
}
