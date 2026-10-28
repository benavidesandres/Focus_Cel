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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Watch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceDim
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusSessionCardShape
import com.focus.app.core.designsystem.FocusSuccess
import com.focus.app.core.designsystem.FocusSuccessSubtle
import com.focus.app.core.designsystem.FocusWarning
import com.focus.app.core.designsystem.FocusWarningSubtle
import com.focus.app.core.designsystem.components.FocusActivitySelector
import com.focus.app.core.designsystem.components.FocusDurationSelector
import com.focus.app.core.designsystem.components.FocusPrimaryButton
import com.focus.app.core.designsystem.components.FocusSecondaryButton
import com.focus.app.core.designsystem.components.SmartwatchLivePreview
import com.focus.app.core.designsystem.components.SmartwatchStatusCard
import com.focus.app.domain.model.FocusActivityType

/**
 * Pantalla principal de Focus adaptada como complemento para Smartwatch (Wear OS).
 *
 * Permite seleccionar la actividad previa:
 * ¿Qué vas a hacer? [ Estudiar ] [ Trabajar ] [ Leer ]
 * Seleccionar duración (ej. 45 min) y monitorear la conexión con el reloj.
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

    LaunchedEffect(navigateToSession) {
        if (navigateToSession) {
            viewModel.onNavigatedToSession()
            onNavigateToSession()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkAccessibilityService()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onActivitySelected = viewModel::onActivitySelected,
        onDurationSelected = viewModel::onDurationSelected,
        onTestSmartwatchVibration = viewModel::onTestSmartwatchVibration,
        onScanForWatch = viewModel::onScanForRealWatch,
        onStartSession = viewModel::onStartSession,
        onNavigateToAppSelection = onNavigateToAppSelection,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onActivitySelected: (FocusActivityType) -> Unit,
    onDurationSelected: (Int) -> Unit,
    onTestSmartwatchVibration: () -> Unit,
    onScanForWatch: () -> Unit,
    onStartSession: () -> Unit,
    onNavigateToAppSelection: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FocusAccentDark.copy(alpha = 0.09f),
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Protege tu atención en tu celular y tu reloj.",
                style = MaterialTheme.typography.bodyLarge,
                color = FocusOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(26.dp))

            // ── Tarjeta de Estado del Smartwatch ────────────────────────────
            SmartwatchStatusCard(
                device = uiState.smartwatchDevice,
                onTestVibration = onTestSmartwatchVibration,
                onScanForWatch = onScanForWatch
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Pregunta Principal: ¿Qué vas a hacer? ────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "¿Qué vas a hacer?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FocusOnSurface
                )
                FocusActivitySelector(
                    selectedActivity = uiState.selectedActivity,
                    onActivitySelected = onActivitySelected
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Selector de duración (45 minutos destacado) ─────────────────
            DurationSection(
                uiState = uiState,
                onDurationSelected = onDurationSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Vista previa interactiva de la pantalla del reloj ────────────
            SmartwatchLivePreview(
                activity = uiState.selectedActivity,
                timeLabel = "${uiState.selectedDurationMinutes}:00",
                progress = 0f
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Tarjeta de estado de sesión / Apps seleccionadas ────────────
            SessionStatusCard(uiState = uiState)

            Spacer(modifier = Modifier.height(28.dp))

            // ── Botones de acción ───────────────────────────────────────────
            ActionSection(
                uiState = uiState,
                onStartSession = onStartSession,
                onNavigateToAppSelection = onNavigateToAppSelection
            )

            // ── Banner de advertencia de accesibilidad ──────────────────────
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Focus",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = FocusOnSurface
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = FocusAccentSubtle,
                border = BorderStroke(1.dp, FocusAccent.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Watch,
                        contentDescription = null,
                        tint = FocusAccentLight,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "COMPANION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusAccentLight
                    )
                }
            }
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

@Composable
private fun DurationSection(
    uiState: HomeUiState,
    onDurationSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Duración de Enfoque",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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

@Composable
private fun SessionStatusCard(uiState: HomeUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = FocusSessionCardShape,
        color = FocusCard,
        border = BorderStroke(1.dp, FocusBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configuración Sincronizada",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = FocusOnSurface
            )

            StatusRow(
                label = "Actividad elegida",
                value = uiState.selectedActivity.displayName,
                isPositive = true
            )

            StatusRow(
                label = "Apps a bloquear en celular",
                value = if (uiState.selectedAppCount == 0) {
                    "Ninguna (Modo Zen solo reloj)"
                } else {
                    "${uiState.selectedAppCount} bloqueadas"
                },
                isPositive = uiState.selectedAppCount > 0
            )

            StatusRow(
                label = "Servicio de bloqueo",
                value = if (uiState.isAccessibilityServiceEnabled) "Activo" else "Deshabilitado",
                isPositive = uiState.isAccessibilityServiceEnabled
            )

            SessionReadinessBadge(
                message = uiState.sessionReadinessMessage,
                isReady = uiState.canStartTimerOnly
            )
        }
    }
}

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
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionSection(
    uiState: HomeUiState,
    onStartSession: () -> Unit,
    onNavigateToAppSelection: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FocusPrimaryButton(
            text = "Iniciar en Celular y Smartwatch",
            onClick = onStartSession,
            enabled = uiState.canStartTimerOnly
        )

        FocusSecondaryButton(
            text = if (uiState.selectedAppCount == 0) {
                "Elegir Apps a Bloquear"
            } else {
                "Apps Bloqueadas (${uiState.selectedAppCount})"
            },
            onClick = onNavigateToAppSelection
        )
    }
}

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
                text = "El servicio de accesibilidad está inactivo. El reloj y el temporizador funcionarán, pero no se bloquearán apps en el celular. Actívalo en Ajustes.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusWarning
            )
        }
    }
}

private fun formatDuration(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes minutos"
        minutes == 60 -> "1 hora"
        minutes % 60 == 0 -> "${minutes / 60} horas"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
