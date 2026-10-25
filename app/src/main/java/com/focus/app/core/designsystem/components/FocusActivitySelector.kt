package com.focus.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentLight
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusChipSelectedBorder
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.domain.model.FocusActivityType

/**
 * Selector principal de actividad de concentración previo a iniciar la sesión.
 *
 * "¿Qué vas a hacer? [ Estudiar ] [ Trabajar ] [ Leer ]"
 *
 * @param selectedActivity Actividad seleccionada actualmente.
 * @param onActivitySelected Callback al seleccionar una actividad.
 * @param modifier Modificador externo.
 */
@Composable
fun FocusActivitySelector(
    selectedActivity: FocusActivityType,
    onActivitySelected: (FocusActivityType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FocusActivityType.entries.forEach { activity ->
                ActivityOptionChip(
                    activity = activity,
                    isSelected = activity == selectedActivity,
                    onClick = { onActivitySelected(activity) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Subtítulo descriptivo de la actividad activa
        Text(
            text = selectedActivity.description,
            style = MaterialTheme.typography.bodySmall,
            color = FocusAccentLight.copy(alpha = 0.9f),
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}

@Composable
private fun ActivityOptionChip(
    activity: FocusActivityType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) FocusAccentSubtle else FocusCard,
        animationSpec = spring(stiffness = 350f),
        label = "ActivityChip_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) FocusChipSelectedBorder else FocusBorder,
        animationSpec = spring(stiffness = 350f),
        label = "ActivityChip_border"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) FocusAccentLight else FocusOnSurfaceVariant,
        animationSpec = spring(stiffness = 350f),
        label = "ActivityChip_text"
    )

    val iconVector: ImageVector = when (activity) {
        FocusActivityType.ESTUDIAR -> Icons.Rounded.School
        FocusActivityType.TRABAJAR -> Icons.Rounded.Work
        FocusActivityType.LEER -> Icons.Rounded.AutoStories
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = if (isSelected) FocusAccent else FocusOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activity.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = textColor
            )
        }
    }
}
