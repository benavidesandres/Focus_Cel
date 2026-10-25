package com.focus.app.domain.model

/**
 * Constantes y definiciones del protocolo de comunicación entre el Celular
 * y el Smartwatch a través de la Wearable Data Layer API.
 */
object SmartwatchProtocol {
    // Rutas para intercambio de mensajes (MessageClient)
    const val PATH_START_SESSION = "/focus/session/start"
    const val PATH_STOP_SESSION = "/focus/session/stop"
    const val PATH_PAUSE_SESSION = "/focus/session/pause"
    const val PATH_RESUME_SESSION = "/focus/session/resume"
    const val PATH_PROGRESS_UPDATE = "/focus/session/progress"
    const val PATH_INACTIVITY_ALERT = "/focus/smartwatch/inactivity_alert"
    const val PATH_REST_ALERT = "/focus/smartwatch/rest_alert"
    const val PATH_HAPTIC_TRIGGER = "/focus/smartwatch/haptic"
    const val PATH_DEVICE_STATUS = "/focus/smartwatch/status"

    // Rutas de sincronización continua de datos (DataClient)
    const val DATA_PATH_ACTIVE_SESSION = "/focus/data/active_session"
    const val DATA_PATH_PREFERENCES = "/focus/data/preferences"

    // Patrones de vibración háptica para el reloj inteligente
    enum class HapticPattern(val description: String, val patternMs: LongArray) {
        SESSION_START(
            description = "Doble pulso firme al iniciar concentración",
            patternMs = longArrayOf(0, 150, 100, 250)
        ),
        HALFWAY_MILESTONE(
            description = "Pulso suave al alcanzar el 50% de la sesión",
            patternMs = longArrayOf(0, 180)
        ),
        INACTIVITY_NUDGE(
            description = "Tres toques suaves recordando estirarse por sedentarismo",
            patternMs = longArrayOf(0, 120, 100, 120, 100, 200)
        ),
        REST_TIME(
            description = "Vibración rítmica y relajante indicando descanso",
            patternMs = longArrayOf(0, 300, 150, 300, 150, 450)
        )
    }
}
