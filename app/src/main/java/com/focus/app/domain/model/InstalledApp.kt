package com.focus.app.domain.model

import android.graphics.drawable.Drawable

/**
 * Modelo de dominio que representa una aplicación instalada en el dispositivo.
 * El nombre del paquete es el identificador estable; no el nombre visible.
 *
 * @param packageName Identificador único del paquete de la app.
 * @param displayName Nombre legible mostrado al usuario.
 * @param icon Drawable del ícono de la app (puede ser nulo si no se pudo cargar).
 */
data class InstalledApp(
    val packageName: String,
    val displayName: String,
    val icon: Drawable? = null
)
