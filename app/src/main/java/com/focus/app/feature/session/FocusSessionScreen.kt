package com.focus.app.feature.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentLight
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBackground
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusSuccess
import com.focus.app.core.designsystem.FocusSuccessSubtle
import com.focus.app.core.designsystem.FocusWarning
import com.focus.app.core.designsystem.FocusWarningSubtle
import com.focus.app.core.designsystem.components.FocusDestructiveButton
import com.focus.app.core.designsystem.components.FocusPrimaryButton
import com.focus.app.core.designsystem.components.FocusProgressIndicator
import com.focus.app.core.designsystem.components.FocusTextButton
import com.focus.app.core.designsystem.components.SmartwatchLivePreview
import com.focus.app.domain.model.FocusActivityType

/**
 * Pantalla de sesión activa de Focus vinculada al Smartwatch.
 *
 * Muestra el temporizador, alerta de falta de movimiento ("Mucho tiempo sin moverte"),
 * la pantalla espejo del reloj, y el modo de descanso guiado ("Te indica cuándo descansar").
 */
@Composable
fun FocusSessionScreen(
    onSessionEnded: () -> Unit,
    viewModel: FocusSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.isActive) {
        viewModel.onEndSessionClicked()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FocusBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Cabecera ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Píldora de actividad elegida y Smartwatch
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActivityPill(activity = uiState.activityType)
                    SmartwatchBadge()
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        uiState.isBreakModeActive -> "Tiempo de Descansar"
                        uiState.isCompleted -> "¡Sesión Completada!"
                        uiState.isInterrupted -> "Sesión Interrumpida"
                        else -> "Modo Concentración Activo"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = FocusOnSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Alerta de Sedentarismo ("Mucho tiempo sin moverte") ──────────
            AnimatedVisibility(
                visible = uiState.isSedentaryAlertActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SedentarismAlertCard(
                    minutesStill = uiState.minutesWithoutMovement,
                    onAcknowledge = viewModel::onAcknowledgeMovement
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Modo Descanso Guiado ("Te indica cuándo descansar") ──────────
            AnimatedVisibility(
                visible = uiState.isBreakModeActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BreakModeCard(
                    breakRemainingLabel = uiState.breakRemainingLabel,
                    onSkipBreak = viewModel::onSkipBreak
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Temporizador circular central ────────────────────────────────
            if (!uiState.isBreakModeActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FocusProgressIndicator(
                        progress = uiState.progress,
                        timeRemainingLabel = uiState.timeRemainingLabel,
                        statusLabel = when {
                            uiState.isCompleted -> "Completado"
                            uiState.isInterrupted -> "Interrumpido"
                            else -> "Protegiendo tu atención"
                        },
                        modifier = Modifier.fillMaxWidth(0.85f),
                        progressColor = if (uiState.isCompleted) FocusSuccess else FocusAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Espejo en vivo de la pantalla del Smartwatch ─────────────────
            SmartwatchLivePreview(
                activity = uiState.activityType,
                timeLabel = if (uiState.isBreakModeActive) uiState.breakRemainingLabel else uiState.timeRemainingLabel,
                progress = uiState.progress,
                isSedentaryWarning = uiState.isSedentaryAlertActive,
                isRestMode = uiState.isBreakModeActive
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Botón de acción principal ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                if (uiState.isActive && !uiState.isBreakModeActive) {
                    FocusDestructiveButton(
                        text = "Terminar Sesión de Enfoque",
                        onClick = { viewModel.onEndSessionClicked() }
                    )
                } else {
                    FocusPrimaryButton(
                        text = "Listo para continuar",
                        onClick = onSessionEnded
                    )
                }
            }
        }
    }

    // ── Diálogo de confirmación ──────────────────────────────────────────────
    if (uiState.showEndConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissEndSessionDialog() },
            containerColor = FocusCard,
            titleContentColor = FocusOnSurface,
            textContentColor = FocusOnSurfaceVariant,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "¿Terminar sesión antes de tiempo?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "El smartwatch y tu celular reanudarán las notificaciones habituales.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                FocusTextButton(
                    text = "Sí, terminar",
                    onClick = { viewModel.onConfirmEndSession() },
                    contentColor = Color(0xFFF87171)
                )
            },
            dismissButton = {
                FocusTextButton(
                    text = "Seguir enfocado",
                    onClick = { viewModel.onDismissEndSessionDialog() },
                    contentColor = FocusAccent
                )
            }
        )
    }
}

@Composable
private fun ActivityPill(activity: FocusActivityType) {
    val icon = when (activity) {
        FocusActivityType.ESTUDIAR -> Icons.Rounded.School
        FocusActivityType.TRABAJAR -> Icons.Rounded.Work
        FocusActivityType.LEER -> Icons.Rounded.AutoStories
    }
    Surface(
        shape = CircleShape,
        color = FocusAccentSubtle,
        border = BorderStroke(1.dp, FocusAccent.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = FocusAccent, modifier = Modifier.size(16.dp))
            Text(text = activity.displayName, style = MaterialTheme.typography.labelMedium, color = FocusAccentLight)
        }
    }
}

@Composable
private fun SmartwatchBadge() {
    Surface(
        shape = CircleShape,
        color = FocusSuccessSubtle,
        border = BorderStroke(1.dp, FocusSuccess.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Watch, contentDescription = null, tint = FocusSuccess, modifier = Modifier.size(14.dp))
            Text(text = "Reloj Sincronizado", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = FocusSuccess)
        }
    }
}

/**
 * Tarjeta de alerta cuando se detecta inmovilidad continua.
 */
@Composable
private fun SedentarismAlertCard(
    minutesStill: Int,
    onAcknowledge: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = FocusWarningSubtle,
        border = BorderStroke(1.5.dp, FocusWarning.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FocusWarning.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsWalk,
                        contentDescription = null,
                        tint = FocusWarning,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "⌚ Llevas mucho tiempo sin moverte",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FocusWarning
                    )
                    Text(
                        text = "Tu Smartwatch vibró suavemente en tu muñeca",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = FocusOnSurfaceVariant
                    )
                }
            }

            Text(
                text = "Llevas aproximadamente $minutesStill minutos enfocado en la misma posición. Ponte de pie, estira los brazos o camina 1 minuto para cuidar tu circulación.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusOnSurface
            )

            FilledTonalButton(
                onClick = onAcknowledge,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = FocusWarning.copy(alpha = 0.15f),
                    contentColor = FocusWarning
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¡Ya me estiré! Continuar sesión",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Tarjeta de descanso guiado ("Te indica cuándo descansar").
 */
@Composable
private fun BreakModeCard(
    breakRemainingLabel: String,
    onSkipBreak: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = FocusSuccessSubtle,
        border = BorderStroke(1.5.dp, FocusSuccess.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(FocusSuccess.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SelfImprovement,
                    contentDescription = null,
                    tint = FocusSuccess,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "🌿 ¡Hora de descansar!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FocusOnSurface
            )

            Text(
                text = breakRemainingLabel,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Light),
                color = FocusSuccess
            )

            Text(
                text = "Tu Smartwatch te ha indicado descansar. Mira a un punto lejano, respira profundamente y relaja la vista antes de tu siguiente bloque.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusOnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            FocusTextButton(
                text = "Omitir descanso y finalizar",
                onClick = onSkipBreak,
                contentColor = FocusSuccess
            )
        }
    }
}
