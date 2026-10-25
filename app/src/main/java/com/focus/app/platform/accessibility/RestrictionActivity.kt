package com.focus.app.platform.accessibility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusBackground
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusTheme
import com.focus.app.core.designsystem.components.FocusDestructiveButton
import com.focus.app.core.designsystem.components.FocusTextButton
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad de restricción mostrada cuando el usuario intenta abrir
 * una aplicación bloqueada durante una sesión activa de Focus.
 *
 * Esta actividad es lanzada por FocusAccessibilityService al detectar
 * que una app restringida pasa al primer plano.
 *
 * Diseño:
 * - Pantalla completa oscura y minimalista para reforzar el mensaje.
 * - Muestra el tiempo restante de sesión.
 * - Permite al usuario regresar o terminar la sesión.
 * - No impide acceso a Ajustes del sistema ni llamadas de emergencia.
 */
@AndroidEntryPoint
class RestrictionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        const val EXTRA_SESSION_ID = "extra_session_id"
    }

    private val viewModel: RestrictionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtiene información de la app bloqueada del intent
        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)

        viewModel.initialize(blockedPackage, sessionId)

        // Extiende el contenido hasta los bordes de la pantalla
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FocusTheme {
                val uiState by viewModel.uiState.collectAsState()

                RestrictionScreen(
                    uiState = uiState,
                    onGoBack = { finish() },
                    onEndSession = {
                        viewModel.endSession()
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * Pantalla de restricción — interfaz de usuario mostrada al bloquear una app.
 */
@Composable
private fun RestrictionScreen(
    uiState: RestrictionUiState,
    onGoBack: () -> Unit,
    onEndSession: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícono de meditación/concentración
            Icon(
                imageVector = Icons.Rounded.SelfImprovement,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = FocusAccent.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Título principal de restricción
            Text(
                text = "You're in Focus mode",
                style = MaterialTheme.typography.headlineMedium,
                color = FocusOnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje explicativo
            Text(
                text = "You selected this app to restrict during your focus session. Stay on track!",
                style = MaterialTheme.typography.bodyLarge,
                color = FocusOnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Tiempo restante de sesión
            AnimatedVisibility(
                visible = uiState.timeRemainingLabel.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Time remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = FocusOnSurfaceVariant
                    )
                    Text(
                        text = uiState.timeRemainingLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = FocusAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Botón para regresar a la pantalla anterior
            com.focus.app.core.designsystem.components.FocusPrimaryButton(
                text = "Go Back",
                onClick = onGoBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón para terminar la sesión (acción destructiva)
            FocusDestructiveButton(
                text = "End Session",
                onClick = onEndSession
            )
        }
    }
}
