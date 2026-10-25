package com.focus.app.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentDark
import com.focus.app.core.designsystem.FocusAccentLight
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBackground
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusCardElevated
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceDim
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusSessionCardShape
import com.focus.app.core.designsystem.FocusSuccess
import com.focus.app.core.designsystem.FocusSuccessSubtle
import com.focus.app.core.designsystem.FocusWarning
import com.focus.app.core.designsystem.FocusWarningSubtle
import com.focus.app.core.designsystem.components.FocusDurationSelector
import com.focus.app.core.designsystem.components.FocusPrimaryButton
import com.focus.app.core.designsystem.components.FocusSecondaryButton

/**
 * Pantalla principal de Focus.
 * Muestra la configuración de sesión, la selección de duración,
 * el estado de las restricciones y los controles de inicio.
 *
 * @param onNavigateToAppSelection Navegar a la pantalla de selección de apps.
 * @param onNavigateToSession Navegar a la pantalla de sesión activa.
 * @param onNavigateToSettings Navegar a la configuración.
 * @param viewModel ViewModel inyectado por Hilt.
 */
@Composable
fun HomeScreen(
    onNavigateToAppSelection: () -> Unit,
    onNavigateToSession: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateToSession by viewModel.navigateToSession.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Navega a la sesión activa cuando el ViewModel lo solicita
    LaunchedEffect(navigateToSession) {
        if (navigateToSession) {
            viewModel.onNavigatedToSession()
            onNavigateToSession()
        }
    }

    // Verifica el estado del servicio de accesibilidad al reanudar la pantalla
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkAccessibilityService()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onDurationSelected = viewModel::onDurationSelected,
        onStartSession = viewModel::onStartSession,
        onNavigateToAppSelection = onNavigateToAppSelection,
        onNavigateToSettings = onNavigateToSettings
    )
}

/**
 * Contenido puro de la pantalla principal (sin ViewModel para facilitar el preview).
 */
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onDurationSelected: (Int) -> Unit,
    onStartSession: () -> Unit,
    onNavigateToAppSelection: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
    ) {
        // Degradado sutil en la parte superior para profundidad visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FocusAccentDark.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // ── Barra superior: Branding + Acceso a Configuración ──────────
            HomeTopBar(onNavigateToSettings = onNavigateToSettings)

            Spacer(modifier = Modifier.height(8.dp))

            // ── Subtítulo ──────────────────────────────────────────────────
            Text(
                text = "Make room for what matters.",
                style = MaterialTheme.typography.bodyLarge,
                color = FocusOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Tarjeta de estado de sesión ─────────────────────────────────
            SessionStatusCard(uiState = uiState)

            Spacer(modifier = Modifier.height(24.dp))

            // ── Selector de duración ────────────────────────────────────────
            DurationSection(
                uiState = uiState,
                onDurationSelected = onDurationSelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Botones de acción ───────────────────────────────────────────
            ActionSection(
                uiState = uiState,
                onStartSession = onStartSession,
                onNavigateToAppSelection = onNavigateToAppSelection
            )

            // ── Banner de accesibilidad (si el servicio no está habilitado) ─
            AnimatedVisibility(
                visible = !uiState.isAccessibilityServiceEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                AccessibilityWarningCard()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Barra superior con el nombre de la app y el botón de configuración.
 */
@Composable
private fun HomeTopBar(
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Focus",
                style = MaterialTheme.typography.headlineSmall,
                color = FocusOnSurface
            )
        }

        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = FocusOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Tarjeta de estado de la sesión que muestra la configuración actual y la disponibilidad.
 */
@Composable
private fun SessionStatusCard(uiState: HomeUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = FocusSessionCardShape,
        color = FocusCard,
        border = BorderStroke(1.dp, FocusBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Encabezado de la tarjeta
            Text(
                text = "Session Configuration",
                style = MaterialTheme.typography.titleMedium,
                color = FocusOnSurface
            )

            // Estado de apps seleccionadas
            StatusRow(
                label = "Apps to restrict",
                value = if (uiState.selectedAppCount == 0) {
                    "None selected"
                } else {
                    "${uiState.selectedAppCount} selected"
                },
                isPositive = uiState.selectedAppCount > 0
            )

            // Estado del servicio de restricción
            StatusRow(
                label = "Restriction service",
                value = if (uiState.isAccessibilityServiceEnabled) {
                    "Active"
                } else {
                    "Disabled"
                },
                isPositive = uiState.isAccessibilityServiceEnabled
            )

            // Resumen del estado de preparación
            SessionReadinessBadge(
                message = uiState.sessionReadinessMessage,
                isReady = uiState.canStartRestrictedSession
            )
        }
    }
}

/**
 * Fila de estado con ícono de verificación y etiqueta de valor.
 */
@Composable
private fun StatusRow(
    label: String,
    value: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = FocusOnSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isPositive) FocusSuccess else FocusOnSurfaceDim,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = if (isPositive) FocusSuccess else FocusOnSurfaceVariant
            )
        }
    }
}

/**
 * Badge que muestra el estado de preparación de la sesión.
 */
@Composable
private fun SessionReadinessBadge(
    message: String,
    isReady: Boolean
) {
    val backgroundColor = if (isReady) FocusSuccessSubtle else FocusAccentSubtle
    val textColor = if (isReady) FocusSuccess else FocusAccentLight
    val borderColor = if (isReady) FocusSuccess.copy(alpha = 0.3f) else FocusAccent.copy(alpha = 0.3f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        AnimatedContent(
            targetState = message,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "SessionReadinessBadge_message"
        ) { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Sección del selector de duración con etiqueta.
 */
@Composable
private fun DurationSection(
    uiState: HomeUiState,
    onDurationSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Focus Duration",
                style = MaterialTheme.typography.titleMedium,
                color = FocusOnSurface
            )
            AnimatedContent(
                targetState = uiState.selectedDurationMinutes,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                },
                label = "DurationSection_selected"
            ) { minutes ->
                Text(
                    text = formatDuration(minutes),
                    style = MaterialTheme.typography.titleMedium,
                    color = FocusAccentLight
                )
            }
        }

        FocusDurationSelector(
            selectedMinutes = uiState.selectedDurationMinutes,
            options = uiState.durationOptions,
            onOptionSelected = onDurationSelected
        )
    }
}

/**
 * Sección de botones de acción: Iniciar sesión y Seleccionar apps.
 */
@Composable
private fun ActionSection(
    uiState: HomeUiState,
    onStartSession: () -> Unit,
    onNavigateToAppSelection: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FocusPrimaryButton(
            text = "Start Focus Session",
            onClick = onStartSession,
            enabled = uiState.canStartTimerOnly
        )

        FocusSecondaryButton(
            text = if (uiState.selectedAppCount == 0) {
                "Select Apps to Restrict"
            } else {
                "Apps Selected (${uiState.selectedAppCount})"
            },
            onClick = onNavigateToAppSelection
        )
    }
}

/**
 * Tarjeta de advertencia de accesibilidad mostrada cuando el servicio está deshabilitado.
 */
@Composable
private fun AccessibilityWarningCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = FocusWarningSubtle,
        border = BorderStroke(1.dp, FocusWarning.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = FocusWarning,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "The accessibility service is disabled. The timer will run, but apps won't be restricted. Enable it in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusWarning
            )
        }
    }
}

/**
 * Formatea los minutos en una cadena legible para el encabezado de la sección.
 */
private fun formatDuration(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes min"
        minutes == 60 -> "1 hour"
        minutes % 60 == 0 -> "${minutes / 60} hours"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
