package com.focus.app.domain.model

/**
 * Modelo de dominio que representa el Smartwatch conectado (Wear OS).
 *
 * @param id Identificador único del nodo Wear OS o del reloj emulado.
 * @param name Nombre visible del smartwatch (ej. "Galaxy Watch", "Pixel Watch", "Focus Watch").
 * @param isConnected Indica si el smartwatch tiene conexión activa Bluetooth/WiFi.
 * @param batteryPercent Nivel de batería actual del smartwatch (0..100).
 * @param isDistractionShieldActive Si el reloj tiene el modo No Molestar / Bloqueo activo.
 * @param hapticVibrationsEnabled Si las vibraciones hápticas en la muñeca están habilitadas.
 * @param sedentarismAlertEnabled Si la detección de inactividad física prolongada está activa.
 * @param sedentarismThresholdMinutes Minutos continuos sin movimiento para lanzar la alerta.
 * @param lastSyncTimestampMs Timestamp del último intercambio de sincronización.
 * @param isSimulated Indica si está en modo complementario interactivo para pruebas.
 */
data class SmartwatchDevice(
    val id: String = "focus_watch_node",
    val name: String = "Focus Smartwatch",
    val isConnected: Boolean = true,
    val batteryPercent: Int = 88,
    val isDistractionShieldActive: Boolean = true,
    val hapticVibrationsEnabled: Boolean = true,
    val sedentarismAlertEnabled: Boolean = true,
    val sedentarismThresholdMinutes: Int = 45,
    val lastSyncTimestampMs: Long = System.currentTimeMillis(),
    val isSimulated: Boolean = false
) {
    val batteryStatusLabel: String
        get() = "$batteryPercent%"

    val connectionLabel: String
        get() = when {
            !isConnected -> "Desconectado"
            isSimulated -> "Simulación Companion Activa"
            else -> "Conectado vía Wear OS"
        }
}

/**
 * Estado general de sincronización con el reloj inteligente.
 */
enum class SmartwatchSyncState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    SYNCING,
    ERROR
}
