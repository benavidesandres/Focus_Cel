package com.focus.app.platform.smartwatch

import com.focus.app.domain.model.SmartwatchProtocol
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Servicio de escucha en segundo plano para eventos provenientes del Smartwatch (Wear OS).
 *
 * Recibe eventos cuando el usuario interactúa directamente con el reloj
 * o cuando los sensores de movimiento del reloj detectan patrones.
 */
@AndroidEntryPoint
class FocusWearableListenerService : WearableListenerService() {

    @Inject
    lateinit var syncManager: SmartwatchSyncManager

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            SmartwatchProtocol.PATH_INACTIVITY_ALERT -> {
                // El sensor del smartwatch detectó inmovilidad prolongada
                syncManager.notifyInactivityDetected()
            }
            SmartwatchProtocol.PATH_STOP_SESSION -> {
                // El usuario finalizó la sesión desde el reloj
                syncManager.notifySessionStopped()
            }
            SmartwatchProtocol.PATH_DEVICE_STATUS -> {
                syncManager.checkConnectedNodes()
            }
        }
    }

    override fun onPeerConnected(peer: Node) {
        syncManager.checkConnectedNodes()
    }

    override fun onPeerDisconnected(peer: Node) {
        syncManager.checkConnectedNodes()
    }
}
