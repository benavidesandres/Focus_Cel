package com.focus.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Familia tipográfica — Inter (cargada desde Google Fonts via bundled) ─────
// Se usa la familia de sistema sans-serif como respaldo hasta que se integren
// los recursos de fuente. En producción, añadir los archivos .ttf de Inter.
val FocusFontFamily = FontFamily.Default

// ── Sistema tipográfico de Focus ─────────────────────────────────────────────
// Basado en Material 3 Typography con ajustes para la identidad visual de Focus

val FocusTypography = Typography(
    // Título de pantalla principal (nombre de la app, timer grande)
    displayLarge = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        color = FocusOnSurface
    ),

    // Timer de sesión activa
    displayMedium = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Thin,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp,
        color = FocusOnSurface
    ),

    // Encabezados principales de tarjeta
    headlineLarge = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = FocusOnSurface
    ),

    // Títulos de sección
    headlineMedium = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = FocusOnSurface
    ),

    // Nombre de la app / branding
    headlineSmall = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = FocusOnSurface
    ),

    // Títulos de tarjeta
    titleLarge = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = FocusOnSurface
    ),

    // Títulos de sección secundaria
    titleMedium = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = FocusOnSurface
    ),

    // Subtítulos y etiquetas de lista
    titleSmall = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = FocusOnSurface
    ),

    // Cuerpo principal de texto
    bodyLarge = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = FocusOnSurfaceVariant
    ),

    // Cuerpo secundario
    bodyMedium = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = FocusOnSurfaceVariant
    ),

    // Texto de soporte y ayuda contextual
    bodySmall = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = FocusOnSurfaceDim
    ),

    // Texto de botón primario
    labelLarge = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = FocusOnSurface
    ),

    // Etiquetas de chip y badge
    labelMedium = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = FocusOnSurfaceVariant
    ),

    // Etiquetas muy pequeñas
    labelSmall = TextStyle(
        fontFamily = FocusFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = FocusOnSurfaceDim
    )
)
