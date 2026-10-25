package com.focus.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentBorder
import com.focus.app.core.designsystem.FocusAccentLight
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusChipSelectedBorder
import com.focus.app.core.designsystem.FocusDurationChipShape
import com.focus.app.core.designsystem.FocusOnSurfaceVariant

/**
 * Selector de duración de sesión con chips para presets comunes.
 * El chip seleccionado se resalta con el acento lavanda.
 *
 * @param selectedMinutes La duración seleccionada actualmente en minutos.
 * @param options Lista de opciones disponibles en minutos.
 * @param onOptionSelected Callback cuando el usuario selecciona una opción.
 * @param modifier Modificador externo.
 */
@Composable
fun FocusDurationSelector(
    selectedMinutes: Int,
    options: List<Int>,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { minutes ->
            FocusDurationChip(
                minutes = minutes,
                isSelected = minutes == selectedMinutes,
                onClick = { onOptionSelected(minutes) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Chip individual de duración.
 * Muestra los minutos y su estado de selección.
 *
 * @param minutes Duración en minutos.
 * @param isSelected Si este chip está seleccionado actualmente.
 * @param onClick Acción al presionar.
 * @param modifier Modificador externo.
 */
@Composable
fun FocusDurationChip(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación de color de fondo al cambiar selección
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) FocusAccentSubtle else FocusCard,
        animationSpec = spring(stiffness = 300f),
        label = "FocusDurationChip_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) FocusChipSelectedBorder else FocusBorder,
        animationSpec = spring(stiffness = 300f),
        label = "FocusDurationChip_border"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) FocusAccentLight else FocusOnSurfaceVariant,
        animationSpec = spring(stiffness = 300f),
        label = "FocusDurationChip_text"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = FocusDurationChipShape,
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = formatDurationChipLabel(minutes),
                style = MaterialTheme.typography.labelLarge.copy(color = textColor),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Formatea los minutos en una etiqueta legible para el chip.
 * Por ejemplo: 25 → "25 min", 60 → "1 hr"
 */
private fun formatDurationChipLabel(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes min"
        minutes == 60 -> "1 hr"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
