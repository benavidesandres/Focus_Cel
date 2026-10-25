package com.focus.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Sistema de formas de Focus ───────────────────────────────────────────────
// Bordes redondeados generosos para una apariencia moderna y premium

val FocusShapes = Shapes(
    // Chips, badges, indicadores pequeños
    extraSmall = RoundedCornerShape(8.dp),

    // Campos de texto, elementos de lista pequeños
    small = RoundedCornerShape(12.dp),

    // Tarjetas medianas, botones estándar
    medium = RoundedCornerShape(16.dp),

    // Tarjetas principales, hojas inferiores
    large = RoundedCornerShape(24.dp),

    // Tarjetas hero, contenedores de pantalla completa
    extraLarge = RoundedCornerShape(32.dp)
)

// ── Formas adicionales para componentes específicos de Focus ─────────────────

// Forma completamente circular (para el indicador de progreso y el punto central)
val FocusCircleShape = RoundedCornerShape(50)

// Forma del botón primario — cápsula para un aspecto moderno
val FocusPrimaryButtonShape = RoundedCornerShape(14.dp)

// Forma del chip de duración
val FocusDurationChipShape = RoundedCornerShape(12.dp)

// Forma de la tarjeta de sesión hero
val FocusSessionCardShape = RoundedCornerShape(28.dp)
