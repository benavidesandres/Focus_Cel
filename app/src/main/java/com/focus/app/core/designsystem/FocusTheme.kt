package com.focus.app.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Esquema de colores oscuro de Focus ──────────────────────────────────────
// Se usa exclusivamente el tema oscuro como apariencia principal de la app

private val FocusDarkColorScheme: ColorScheme = darkColorScheme(
    // Colores primarios — acento lavanda
    primary = FocusAccent,
    onPrimary = Color(0xFF1A0060),
    primaryContainer = FocusAccentDark,
    onPrimaryContainer = FocusAccentLight,

    // Colores secundarios — tonos neutros cálidos
    secondary = Color(0xFF7B7A9E),
    onSecondary = Color(0xFF14133A),
    secondaryContainer = Color(0xFF2A2A3D),
    onSecondaryContainer = FocusOnSurfaceVariant,

    // Colores terciarios
    tertiary = Color(0xFF6ECFCF),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF004F4F),
    onTertiaryContainer = Color(0xFF9FEFEF),

    // Colores de error
    error = FocusError,
    onError = Color(0xFF2D0000),
    errorContainer = Color(0xFF3B0000),
    onErrorContainer = Color(0xFFFFB3B0),

    // Fondo y superficie
    background = FocusBackground,
    onBackground = FocusOnSurface,
    surface = FocusSurface,
    onSurface = FocusOnSurface,
    surfaceVariant = FocusCard,
    onSurfaceVariant = FocusOnSurfaceVariant,

    // Bordes y contornos
    outline = FocusBorder,
    outlineVariant = FocusBorderActive,

    // Superficie inversa
    inverseSurface = FocusOnSurface,
    inverseOnSurface = FocusBackground,
    inversePrimary = FocusAccentDark,

    // Scrim para diálogos y hojas modales
    scrim = Color(0x80000000)
)

// ── Composable principal del tema Focus ──────────────────────────────────────

/**
 * Aplica el sistema de diseño de Focus a la jerarquía de composables hija.
 * Siempre usa el tema oscuro como apariencia principal.
 *
 * @param content El contenido al que se aplica el tema.
 */
@Composable
fun FocusTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FocusDarkColorScheme
    val view = LocalView.current

    // Configura la barra de estado y la barra de navegación para que
    // sean transparentes y usen íconos claros (tema oscuro)
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FocusTypography,
        shapes = FocusShapes,
        content = content
    )
}
