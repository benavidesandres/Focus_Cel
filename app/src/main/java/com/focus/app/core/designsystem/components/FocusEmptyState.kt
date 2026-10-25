package com.focus.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant

/**
 * Estado vacío genérico de Focus con ícono, título y descripción.
 * Se usa cuando no hay contenido disponible para mostrar.
 *
 * @param icon Vector del ícono representativo.
 * @param title Título principal del estado vacío.
 * @param description Descripción explicativa opcional.
 * @param action Composable de acción opcional (botón de reintentar, etc.).
 * @param modifier Modificador externo.
 */
@Composable
fun FocusEmptyState(
    icon: ImageVector,
    title: String,
    description: String? = null,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícono principal con opacidad reducida para un aspecto sutil
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = FocusAccent.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título del estado vacío
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = FocusOnSurface,
            textAlign = TextAlign.Center
        )

        // Descripción opcional
        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = FocusOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Acción opcional (botón de reintentar, etc.)
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

/**
 * Banner de permiso que informa al usuario sobre permisos requeridos.
 * Muestra información clara antes de dirigir al usuario a Ajustes.
 *
 * @param title Título del banner.
 * @param description Explicación del permiso requerido.
 * @param actionLabel Texto del botón de acción.
 * @param onAction Acción al presionar el botón (normalmente abre Ajustes).
 * @param modifier Modificador externo.
 */
@Composable
fun FocusPermissionBanner(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = FocusOnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = FocusOnSurfaceVariant
            )
        }
        FocusTextButton(
            text = actionLabel,
            onClick = onAction
        )
    }
}
