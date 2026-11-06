package com.focus.app.domain.model

/**
 * Fase en tiempo real de la sesión, publicada por el gestor de sesiones.
 * Es la única fuente de verdad para la UI, el bloqueo de apps y la notificación.
 */
sealed interface SessionPhase {

    /** No hay sesión en curso. */
    data object Idle : SessionPhase

    /**
     * El temporizador corre y las apps seleccionadas están bloqueadas.
     *
     * @param session Sesión en curso.
     * @param remainingMillis Milisegundos restantes en el último tick.
     */
    data class Focusing(
        val session: FocusSession,
        val remainingMillis: Long
    ) : SessionPhase

    /**
     * La sesión terminó y transcurre el descanso guiado. Las apps ya no están bloqueadas.
     *
     * @param session Sesión que acaba de terminar.
     * @param breakEndTimestampMs Momento en que termina el descanso (epoch).
     * @param remainingMillis Milisegundos de descanso restantes en el último tick.
     */
    data class Resting(
        val session: FocusSession,
        val breakEndTimestampMs: Long,
        val remainingMillis: Long
    ) : SessionPhase

    /**
     * La sesión se completó y espera a que el usuario vea el resumen.
     *
     * @param session Sesión completada.
     */
    data class Completed(val session: FocusSession) : SessionPhase
}
