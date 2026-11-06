package com.focus.app.core.util

import java.util.Locale

/**
 * Formatea una duración en milisegundos como MM:SS, o HH:MM:SS a partir de una hora.
 *
 * @param millis Duración en milisegundos.
 * @return Etiqueta lista para mostrar en un temporizador.
 */
fun formatCountdown(millis: Long): String {
    // Redondea hacia arriba para que el contador no muestre 00:00 antes de terminar
    val totalSeconds = (millis + 999L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
        String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}
