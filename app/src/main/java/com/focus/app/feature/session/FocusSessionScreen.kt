package com.focus.app.feature.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focus.app.R
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentLight
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBackground
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
 * Muestra el temporizador, la alerta de falta de movimiento, la pantalla espejo
 * del reloj y el descanso guiado. Se cierra sola cuando ya no hay sesión en curso,
 * sin importar si terminó desde esta pantalla, el bloqueo o el reloj.
 */
@Composable
fun FocusSessionScreen(
    onSessionEnded: () -> Unit,
    viewModel: FocusSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isFocusing = uiState.stage == SessionStage.FOCUSING
    val isResting = uiState.stage == SessionStage.RESTING
    val isCompleted = uiState.stage == SessionStage.COMPLETED

    LaunchedEffect(uiState.stage) {
        if (uiState.stage == SessionStage.IDLE) onSessionEnded()
    }

    BackHandler {
        when (uiState.stage) {
            SessionStage.FOCUSING -> viewModel.onEndSessionClicked()
            SessionStage.RESTING -> viewModel.onSkipBreak()
            SessionStage.COMPLETED -> viewModel.onDone()
            SessionStage.IDLE -> onSessionEnded()
        }
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
                    SmartwatchBadge(isConnected = uiState.isWatchConnected)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(
                        when {
                            isResting -> R.string.session_title_resting
                            isCompleted -> R.string.session_title_completed
                            else -> R.string.session_title_focusing
                        }
                    ),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = FocusOnSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Alerta de inactividad física ─────────────────────────────────
            AnimatedVisibility(
                visible = isFocusing && uiState.isInactivityAlertActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                InactivityAlertCard(
                    minutesStill = uiState.minutesWithoutMovement,
                    onAcknowledge = viewModel::onAcknowledgeMovement
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Descanso guiado ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = isResting,
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
            if (!isResting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FocusProgressIndicator(
                        progress = uiState.progress,
                        timeRemainingLabel = uiState.timeRemainingLabel,
                        statusLabel = stringResource(
                            if (isCompleted) {
                                R.string.session_status_completed
                            } else {
                                R.string.session_status_focusing
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(0.85f),
                        progressColor = if (isCompleted) FocusSuccess else FocusAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Espejo en vivo de la pantalla del Smartwatch ─────────────────
            SmartwatchLivePreview(
                activity = uiState.activityType,
                timeLabel = if (isResting) uiState.breakRemainingLabel else uiState.timeRemainingLabel,
                progress = uiState.progress,
                isInactivityWarning = isFocusing && uiState.isInactivityAlertActive,
                isRestMode = isResting
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Botón de acción principal ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                if (isFocusing) {
                    FocusDestructiveButton(
                        text = stringResource(R.string.session_end_button),
                        onClick = viewModel::onEndSessionClicked
                    )
                } else if (isCompleted) {
                    FocusPrimaryButton(
                        text = stringResource(R.string.session_done_button),
                        onClick = viewModel::onDone
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
                    text = stringResource(R.string.session_end_confirm_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.session_end_confirm_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                FocusTextButton(
                    text = stringResource(R.string.session_end_confirm_yes),
                    onClick = { viewModel.onConfirmEndSession() },
                    contentColor = Color(0xFFF87171)
                )
            },
            dismissButton = {
                FocusTextButton(
                    text = stringResource(R.string.session_end_confirm_no),
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
        FocusActivityType.STUDY -> Icons.Rounded.School
        FocusActivityType.WORK -> Icons.Rounded.Work
        FocusActivityType.READ -> Icons.Rounded.AutoStories
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
private fun SmartwatchBadge(isConnected: Boolean) {
    val contentColor = if (isConnected) FocusSuccess else FocusOnSurfaceVariant

    Surface(
        shape = CircleShape,
        color = if (isConnected) FocusSuccessSubtle else FocusCard,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Watch, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Text(
                text = stringResource(
                    if (isConnected) R.string.session_watch_synced else R.string.session_watch_not_connected
                ),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
        }
    }
}

/**
 * Tarjeta de alerta cuando se detecta inmovilidad continua.
 */
@Composable
private fun InactivityAlertCard(
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
                        imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                        contentDescription = null,
                        tint = FocusWarning,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.session_inactivity_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FocusWarning
                    )
                    Text(
                        text = stringResource(R.string.session_inactivity_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = FocusOnSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.session_inactivity_message, minutesStill),
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
                    text = stringResource(R.string.session_inactivity_acknowledge),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Tarjeta de descanso guiado al terminar la sesión.
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
                text = stringResource(R.string.session_break_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FocusOnSurface
            )

            Text(
                text = breakRemainingLabel,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Light),
                color = FocusSuccess
            )

            Text(
                text = stringResource(R.string.session_break_message),
                style = MaterialTheme.typography.bodySmall,
                color = FocusOnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            FocusTextButton(
                text = stringResource(R.string.session_break_skip),
                onClick = onSkipBreak,
                contentColor = FocusSuccess
            )
        }
    }
}
