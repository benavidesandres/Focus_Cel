package com.focus.app.core.designsystem.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focus.app.core.designsystem.FocusCard

/**
 * Componente que muestra el ícono de una aplicación instalada.
 * Convierte el Drawable del sistema a ImageBitmap para Compose.
 * Muestra un ícono de Android genérico si el drawable es nulo.
 *
 * @param drawable El drawable del ícono de la app obtenido de PackageManager.
 * @param contentDescription Descripción para accesibilidad.
 * @param size Tamaño del ícono.
 * @param cornerRadius Radio de los bordes redondeados.
 */
@Composable
fun FocusAppIcon(
    drawable: Drawable?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    cornerRadius: Dp = 12.dp
) {
    // Convierte el Drawable a ImageBitmap de forma eficiente
    val imageBitmap = rememberDrawableAsBitmap(drawable)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(FocusCard)
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.size(size)
            )
        } else {
            // Ícono de respaldo cuando no se puede cargar el ícono real
            Icon(
                imageVector = Icons.Rounded.Android,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.6f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Convierte un Drawable en ImageBitmap para usar en Compose.
 * El resultado se memoriza para evitar conversiones repetidas.
 *
 * @param drawable El drawable a convertir.
 * @return ImageBitmap listo para Compose, o null si no se puede convertir.
 */
@Composable
private fun rememberDrawableAsBitmap(drawable: Drawable?): ImageBitmap? {
    return remember(drawable) {
        when {
            drawable == null -> null
            drawable is BitmapDrawable && drawable.bitmap != null -> {
                drawable.bitmap.asImageBitmap()
            }
            else -> {
                // Renderiza cualquier tipo de Drawable en un Bitmap
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap.asImageBitmap()
            }
        }
    }
}
