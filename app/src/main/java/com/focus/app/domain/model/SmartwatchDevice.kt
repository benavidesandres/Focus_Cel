package com.focus.app.domain.model

/**
 * Estado de la conexión con el reloj inteligente.
 */
enum class SmartwatchConnectionState {
    /** Se está consultando la Wearable Data Layer. */
    SEARCHING,

    /** No hay ningún reloj alcanzable. */
    DISCONNECTED,

    /** Hay un reloj emparejado, pero no tiene instalada la app Focus para Wear OS. */
    MISSING_WEAR_APP,

    /** Hay un reloj alcanzable con la app Focus para Wear OS. */
    CONNECTED
}

/**
 * Modelo de dominio que representa el reloj Wear OS detectado.
 * Solo contiene datos reales: un valor nulo significa que el reloj aún no lo informó.
 *
 * @param id Identificador del nodo Wear OS, o null si no hay reloj.
 * @param name Nombre visible del reloj, o null si no hay reloj.
 * @param connectionState Estado actual de la conexión.
 * @param batteryPercent Nivel de batería (0..100), o null si se desconoce.
 * @param isNearby Si el reloj está conectado directamente (Bluetooth) y no por la nube.
 */
data class SmartwatchDevice(
    val id: String? = null,
    val name: String? = null,
    val connectionState: SmartwatchConnectionState = SmartwatchConnectionState.SEARCHING,
    val batteryPercent: Int? = null,
    val isNearby: Boolean = false
) {
    /** Indica si el reloj puede recibir órdenes de Focus. */
    val isConnected: Boolean
        get() = connectionState == SmartwatchConnectionState.CONNECTED
}
