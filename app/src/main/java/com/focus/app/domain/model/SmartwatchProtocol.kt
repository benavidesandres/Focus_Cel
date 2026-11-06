package com.focus.app.domain.model

/**
 * Contrato de comunicación entre el teléfono y el reloj sobre la Wearable Data Layer.
 *
 * IMPORTANTE: la Data Layer solo entrega datos entre apps con el mismo applicationId y
 * la misma firma, por lo que la app del reloj debe compilarse como "com.focus.app".
 *
 * Teléfono → reloj: [PATH_START_SESSION], [PATH_STOP_SESSION], [PATH_PROGRESS_UPDATE],
 * [PATH_INACTIVITY_ALERT], [PATH_REST_ALERT], [PATH_HAPTIC_TRIGGER], [PATH_PONG] y
 * [PATH_DEVICE_STATUS] (vacío, solicita el estado).
 *
 * Reloj → teléfono: [PATH_START_SESSION] (`durationMinutes` y `activityId`, ambos opcionales),
 * [PATH_STOP_SESSION], [PATH_ACCELEROMETER_EVENT], [PATH_DISMISS_ALERT], [PATH_BATTERY]
 * (`battery`), [PATH_DEVICE_STATUS] (`model`, `battery`) y [PATH_PING].
 *
 * Todas las cargas útiles son JSON en UTF-8.
 */
object SmartwatchProtocol {
    // Capacidades declaradas para descubrimiento bidireccional (CapabilityClient)
    const val CAPABILITY_WEAR_APP = "focus_wear_app"

    // Rutas para intercambio de mensajes (MessageClient)
    const val PATH_START_SESSION = "/focus/session/start"
    const val PATH_STOP_SESSION = "/focus/session/stop"
    const val PATH_PROGRESS_UPDATE = "/focus/session/progress"
    const val PATH_INACTIVITY_ALERT = "/focus/smartwatch/inactivity_alert"
    const val PATH_REST_ALERT = "/focus/smartwatch/rest_alert"
    const val PATH_HAPTIC_TRIGGER = "/focus/smartwatch/haptic"
    const val PATH_DEVICE_STATUS = "/focus/smartwatch/status"
    const val PATH_BATTERY = "/focus/smartwatch/battery"
    const val PATH_ACCELEROMETER_EVENT = "/focus/smartwatch/movement"
    const val PATH_DISMISS_ALERT = "/focus/smartwatch/dismiss_alert"
    const val PATH_PING = "/focus/smartwatch/ping"
    const val PATH_PONG = "/focus/smartwatch/pong"

    // Rutas de sincronización continua de datos (DataClient)
    const val DATA_PATH_ACTIVE_SESSION = "/focus/data/active_session"
    const val DATA_PATH_PREFERENCES = "/focus/data/preferences"

    /** Patrones de vibración háptica; [timingsMs] alterna pausa y vibración. */
    enum class HapticPattern(val timingsMs: LongArray) {
        // Doble pulso firme al iniciar la concentración
        SESSION_START(longArrayOf(0, 150, 100, 250)),

        // Pulso suave al alcanzar el 50% de la sesión
        HALFWAY_MILESTONE(longArrayOf(0, 180)),

        // Tres toques suaves que recuerdan estirarse
        INACTIVITY_NUDGE(longArrayOf(0, 120, 100, 120, 100, 200)),

        // Vibración rítmica y relajante que indica el descanso
        REST_TIME(longArrayOf(0, 300, 150, 300, 150, 450))
    }
}
