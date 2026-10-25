package com.focus.app.feature.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focus.app.R
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBackground
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusSuccess
import com.focus.app.core.designsystem.FocusSuccessSubtle
import com.focus.app.core.designsystem.components.FocusDestructiveButton
import com.focus.app.core.designsystem.components.FocusPrimaryButton
import com.focus.app.core.designsystem.components.FocusProgressIndicator
import com.focus.app.core.designsystem.components.FocusTextButton

/**
 * Pantalla de sesión activa de Focus.
 *
 * Muestra el temporizador circular animado, el número de aplicaciones restringidas,
 * y permite terminar la sesión con confirmación para evitar interrupciones accidentales.
 *
 * @param onSessionEnded Callback invocado al terminar la sesión o regresar al inicio.
 * @param viewModel ViewModel de la sesión inyectado por Hilt.
 */
@Composable
fun FocusSessionScreen(
    onSessionEnded: () -> Unit,
    viewModel: FocusSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Si la sesión está activa y el usuario presiona Atrás en el dispositivo,
    // se solicita confirmación en lugar de salir silenciosamente.
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
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.isCompleted) {
                        stringResource(R.string.session_status_completed)
                    } else if (uiState.isInterrupted) {
                        stringResource(R.string.session_status_interrupted)
                    } else {
                        stringResource(R.string.session_title)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = FocusOnSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Etiqueta de estado o modo
                SessionModeBadge(
                    restrictedAppsCount = uiState.restrictedAppsCount,
                    isCompleted = uiState.isCompleted
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Temporizador circular central ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                FocusProgressIndicator(
                    progress = uiState.progress,
                    timeRemainingLabel = uiState.timeRemainingLabel,
                    statusLabel = when {
                        uiState.isCompleted -> stringResource(R.string.session_great_job)
                        uiState.isInterrupted -> stringResource(R.string.session_status_interrupted)
                        else -> stringResource(R.string.session_status_active)
                    },
                    modifier = Modifier.fillMaxWidth(0.85f),
                    progressColor = if (uiState.isCompleted) FocusSuccess else FocusAccent
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Tarjeta contextual inferior ──────────────────────────────────
            AnimatedContent(
                targetState = uiState.isCompleted,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SessionInfoCard"
            ) { isDone ->
                if (isDone) {
                    CompletionCard(
                        durationMinutes = uiState.durationMinutes,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ActiveSessionInfoCard(
                        restrictedAppsCount = uiState.restrictedAppsCount,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Botón de acción principal ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                if (uiState.isActive) {
                    FocusDestructiveButton(
                        text = stringResource(R.string.session_end_button),
                        onClick = { viewModel.onEndSessionClicked() }
                    )
                } else {
                    FocusPrimaryButton(
                        text = stringResource(R.string.session_done_button),
                        onClick = onSessionEnded
                    )
                }
            }
        }
    }

    // ── Diálogo de confirmación para terminar sesión ─────────────────────────
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

/**
 * Insignia que describe el modo activo (restringido vs solo temporizador).
 */
@Composable
private fun SessionModeBadge(
    restrictedAppsCount: Int,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val (bgColor, contentColor, icon, text) = when {
        isCompleted -> Quadruple(
            FocusSuccessSubtle,
            FocusSuccess,
            Icons.Rounded.CheckCircle,
            "Focus session completed"
        )
        restrictedAppsCount > 0 -> Quadruple(
            FocusAccentSubtle,
            FocusAccent,
            Icons.Rounded.Lock,
            stringResource(R.string.session_restricted_apps, restrictedAppsCount)
        )
        else -> Quadruple(
            FocusCard,
            FocusOnSurfaceVariant,
            Icons.Rounded.Timer,
            "Timer-only session"
        )
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * Tarjeta informativa mostrada durante la sesión activa.
 */
@Composable
private fun ActiveSessionInfoCard(
    restrictedAppsCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = FocusCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, FocusBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FocusAccentSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SelfImprovement,
                    contentDescription = null,
                    tint = FocusAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (restrictedAppsCount > 0) {
                        "Distractions are blocked"
                    } else {
                        "Time to concentrate"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = FocusOnSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (restrictedAppsCount > 0) {
                        "Opening restricted apps will show your focus reminder screen."
                    } else {
                        "No apps are restricted for this session."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusOnSurfaceVariant
                )
            }
        }
    }
}

/**
 * Tarjeta de felicitación mostrada al completar la sesión.
 */
@Composable
private fun CompletionCard(
    durationMinutes: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = FocusSuccessSubtle,
        border = androidx.compose.foundation.BorderStroke(1.dp, FocusSuccess.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = FocusSuccess,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.session_great_job),
                style = MaterialTheme.typography.titleMedium,
                color = FocusOnSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.session_completed_message, "$durationMinutes min"),
                style = MaterialTheme.typography.bodyMedium,
                color = FocusOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
