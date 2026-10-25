package com.focus.app.domain.model

/**
 * Modelo de dominio que representa una sesión de enfoque.
 * Usa timestamps absolutos para que el temporizador sea robusto frente
 * a la destrucción del proceso, recreación de la actividad y navegación.
 *
 * @param id Identificador único de la sesión (timestamp de inicio).
 * @param startTimestampMs Tiempo de inicio en milisegundos (epoch).
 * @param endTimestampMs Tiempo de finalización previsto en milisegundos (epoch).
 * @param durationMinutes Duración configurada de la sesión en minutos.
 * @param selectedPackageNames Conjunto de paquetes seleccionados para restricción.
 * @param status Estado actual del ciclo de vida de la sesión.
 */
data class FocusSession(
    val id: Long,
    val startTimestampMs: Long,
    val endTimestampMs: Long,
    val durationMinutes: Int,
    val selectedPackageNames: Set<String>,
    val status: SessionStatus
) {
    /**
     * Calcula los milisegundos restantes basándose en el timestamp actual.
     * Siempre no negativo; devuelve 0 si la sesión ya expiró.
     *
     * @param nowMs Timestamp actual en milisegundos.
     * @return Milisegundos restantes.
     */
    fun remainingMillis(nowMs: Long): Long {
        return (endTimestampMs - nowMs).coerceAtLeast(0L)
    }

    /**
     * Determina si la sesión ha expirado basándose en el timestamp actual.
     *
     * @param nowMs Timestamp actual en milisegundos.
     * @return true si el tiempo de finalización ya pasó.
     */
    fun isExpired(nowMs: Long): Boolean {
        return nowMs >= endTimestampMs
    }

    /**
     * Fracción de progreso para el indicador circular (0f = tiempo agotado, 1f = recién iniciada).
     * Se invierte para que el arco se reduzca a medida que avanza el tiempo.
     *
     * @param nowMs Timestamp actual en milisegundos.
     * @return Fracción entre 0f y 1f.
     */
    fun progressFraction(nowMs: Long): Float {
        val totalMs = (endTimestampMs - startTimestampMs).toFloat()
        if (totalMs <= 0f) return 0f
        val remaining = remainingMillis(nowMs).toFloat()
        return (remaining / totalMs).coerceIn(0f, 1f)
    }
}
