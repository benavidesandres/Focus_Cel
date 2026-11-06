package com.focus.app.domain.model

/**
 * Órdenes que el reloj envía al teléfono.
 */
sealed interface SmartwatchCommand {

    /**
     * El usuario inició una sesión desde el reloj.
     *
     * @param durationMinutes Duración pedida, o null para usar la preferida en el teléfono.
     * @param activityType Actividad pedida, o null para usar la actividad por defecto.
     */
    data class StartSession(
        val durationMinutes: Int?,
        val activityType: FocusActivityType?
    ) : SmartwatchCommand

    /** El usuario detuvo la sesión desde el reloj. */
    data object StopSession : SmartwatchCommand

    /** Los sensores del reloj detectaron movimiento físico. */
    data object MovementDetected : SmartwatchCommand

    /** El usuario descartó la alerta de inactividad desde el reloj. */
    data object DismissInactivityAlert : SmartwatchCommand
}
