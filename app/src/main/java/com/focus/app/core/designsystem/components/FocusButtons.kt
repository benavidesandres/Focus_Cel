package com.focus.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentDark
import com.focus.app.core.designsystem.FocusBackground
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusPrimaryButtonShape

/**
 * Botón primario del sistema de diseño Focus.
 * Tiene fondo de acento lavanda con animación de escala al presionar.
 *
 * @param text El texto del botón.
 * @param onClick Acción al presionar.
 * @param modifier Modificador externo.
 * @param enabled Si el botón está habilitado.
 */
@Composable
fun FocusPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animación de escala sutil al presionar
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "FocusPrimaryButton_scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        enabled = enabled,
        shape = FocusPrimaryButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = FocusAccent,
            contentColor = FocusBackground,
            disabledContainerColor = FocusAccent.copy(alpha = 0.3f),
            disabledContentColor = FocusBackground.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (enabled) FocusBackground else FocusBackground.copy(alpha = 0.4f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Botón secundario del sistema de diseño Focus.
 * Contorno lavanda sobre fondo transparente.
 *
 * @param text El texto del botón.
 * @param onClick Acción al presionar.
 * @param modifier Modificador externo.
 * @param enabled Si el botón está habilitado.
 */
@Composable
fun FocusSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "FocusSecondaryButton_scale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        enabled = enabled,
        shape = FocusPrimaryButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = FocusAccent,
            disabledContentColor = FocusAccent.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) FocusAccent.copy(alpha = 0.6f) else FocusBorder
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (enabled) FocusAccent else FocusAccent.copy(alpha = 0.4f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Botón de texto (ghost) para acciones terciarias.
 *
 * @param text El texto del botón.
 * @param onClick Acción al presionar.
 * @param modifier Modificador externo.
 * @param contentColor Color del texto.
 */
@Composable
fun FocusTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = FocusAccent
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(color = contentColor)
        )
    }
}

/**
 * Botón de peligro (destructivo) para acciones como "Terminar sesión".
 *
 * @param text El texto del botón.
 * @param onClick Acción al presionar.
 * @param modifier Modificador externo.
 */
@Composable
fun FocusDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = FocusPrimaryButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFFF87171)
        ),
        border = BorderStroke(1.dp, Color(0x60F87171))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFFF87171)),
            textAlign = TextAlign.Center
        )
    }
}
