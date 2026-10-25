package com.focus.app.domain.model

/**
 * Tipos de actividad para sesiones de enfoque en Smartwatch y Móvil.
 *
 * Cada actividad personaliza la experiencia de concentración:
 * - [ESTUDIAR]: Alta exigencia cognitiva, pausas recomendadas de 10 min.
 * - [TRABAJAR]: Productividad profesional, bloqueo riguroso de distracciones.
 * - [LEER]: Estado inmersivo y de bajo estrés con detección sutil de inactividad.
 */
enum class FocusActivityType(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultDurationMinutes: Int,
    val inactivityThresholdMinutes: Int,
    val recommendedRestMinutes: Int
) {
    ESTUDIAR(
        id = "study",
        displayName = "Estudiar",
        description = "Bloqueo riguroso de redes y alertas de repaso",
        defaultDurationMinutes = 45,
        inactivityThresholdMinutes = 40,
        recommendedRestMinutes = 10
    ),
    TRABAJAR(
        id = "work",
        displayName = "Trabajar",
        description = "Máxima productividad sin interrupciones",
        defaultDurationMinutes = 45,
        inactivityThresholdMinutes = 45,
        recommendedRestMinutes = 5
    ),
    LEER(
        id = "read",
        displayName = "Leer",
        description = "Lectura profunda con postura y recordatorios suaves",
        defaultDurationMinutes = 30,
        inactivityThresholdMinutes = 30,
        recommendedRestMinutes = 5
    );

    companion object {
        fun fromId(id: String?): FocusActivityType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: TRABAJAR
        }
    }
}
