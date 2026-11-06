package com.focus.app.domain.model

/**
 * Tipos de actividad para sesiones de enfoque en Smartwatch y Móvil.
 *
 * Cada actividad personaliza la experiencia de concentración:
 * - [STUDY]: Alta exigencia cognitiva, pausas recomendadas de 10 min.
 * - [WORK]: Productividad profesional, bloqueo riguroso de distracciones.
 * - [READ]: Estado inmersivo y de bajo estrés.
 *
 * @param id Identificador estable usado en la persistencia y en el protocolo del reloj.
 * @param displayName Nombre visible de la actividad.
 * @param description Descripción corta mostrada en el selector.
 * @param recommendedRestMinutes Minutos de descanso guiado al terminar la sesión.
 */
enum class FocusActivityType(
    val id: String,
    val displayName: String,
    val description: String,
    val recommendedRestMinutes: Int
) {
    STUDY(
        id = "study",
        displayName = "Study",
        description = "Strict blocking of social apps and review reminders",
        recommendedRestMinutes = 10
    ),
    WORK(
        id = "work",
        displayName = "Work",
        description = "Maximum productivity without interruptions",
        recommendedRestMinutes = 5
    ),
    READ(
        id = "read",
        displayName = "Read",
        description = "Deep reading with gentle posture reminders",
        recommendedRestMinutes = 5
    );

    companion object {
        /** Resuelve una actividad por su identificador; usa [WORK] si es desconocido. */
        fun fromId(id: String?): FocusActivityType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: WORK
        }
    }
}
