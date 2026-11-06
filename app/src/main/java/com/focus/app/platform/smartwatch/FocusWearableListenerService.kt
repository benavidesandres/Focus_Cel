package com.focus.app.platform.smartwatch

import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Punto de entrada de los eventos del reloj, tanto con la app abierta como cerrada.
 * Google Play Services crea el proceso si hace falta, así que no se necesitan listeners dinámicos.
 */
@AndroidEntryPoint
class FocusWearableListenerService : WearableListenerService() {

    @Inject
    lateinit var syncManager: SmartwatchSyncManager

    override fun onMessageReceived(messageEvent: MessageEvent) {
        syncManager.onMessageReceived(
            sourceNodeId = messageEvent.sourceNodeId,
            path = messageEvent.path,
            data = messageEvent.data
        )
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        syncManager.refreshConnection()
    }
}
