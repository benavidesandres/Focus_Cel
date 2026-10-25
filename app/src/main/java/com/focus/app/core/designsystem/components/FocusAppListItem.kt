package com.focus.app.core.designsystem.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant

/**
 * Elemento de lista para mostrar una aplicación instalada con su ícono,
 * nombre y control de selección.
 *
 * @param appName Nombre visible de la aplicación.
 * @param packageName Nombre del paquete (mostrado como subtítulo).
 * @param icon Drawable del ícono de la aplicación.
 * @param isSelected Si la aplicación está seleccionada actualmente.
 * @param onClick Acción al presionar el elemento.
 * @param modifier Modificador externo.
 */
@Composable
fun FocusAppListItem(
    appName: String,
    packageName: String,
    icon: Drawable?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación de color de fondo al seleccionar/deseleccionar
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) FocusAccentSubtle else FocusCard,
        animationSpec = spring(stiffness = 400f),
        label = "FocusAppListItem_bg"
    )

    val checkColor by animateColorAsState(
        targetValue = if (isSelected) FocusAccent else FocusOnSurfaceVariant.copy(alpha = 0.4f),
        animationSpec = spring(stiffness = 400f),
        label = "FocusAppListItem_check"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ícono de la aplicación
            FocusAppIcon(
                drawable = icon,
                contentDescription = appName,
                size = 48.dp
            )

            // Nombre y paquete de la aplicación
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleSmall,
                    color = FocusOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Ícono de selección con animación de color
            Icon(
                imageVector = if (isSelected) {
                    Icons.Rounded.CheckCircle
                } else {
                    Icons.Rounded.RadioButtonUnchecked
                },
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = checkColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
