package com.focus.app.platform.accessibility

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focus.app.R
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusBackground
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusTheme
import com.focus.app.core.designsystem.components.FocusDestructiveButton
import com.focus.app.core.designsystem.components.FocusPrimaryButton
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
 * - Muestra la app bloqueada y el tiempo restante de sesión.
 * - Salir siempre lleva al inicio: volver atrás mostraría de nuevo la app bloqueada.
 * - Se cierra sola si la sesión termina mientras está visible.
 */
@AndroidEntryPoint
class RestrictionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }

    private val viewModel: RestrictionViewModel by viewModels()

    private var blockedAppName by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blockedAppName = resolveAppName(intent)

        // Extiende el contenido hasta los bordes de la pantalla
        WindowCompat.setDecorFitsSystemWindows(window, false)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = goToHomeScreen()
            }
        )

        setContent {
            FocusTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.isSessionActive) {
                    if (!uiState.isSessionActive) finish()
                }

                RestrictionScreen(
                    uiState = uiState,
                    blockedAppName = blockedAppName,
                    onGoBack = ::goToHomeScreen,
                    onEndSession = viewModel::onEndSession
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // La actividad es singleTop: otra app bloqueada puede reutilizar esta instancia
        setIntent(intent)
        blockedAppName = resolveAppName(intent)
    }

    private fun goToHomeScreen() {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    private fun resolveAppName(intent: Intent): String {
        val packageName = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            ""
        }
    }
}

/**
 * Pantalla de restricción — interfaz de usuario mostrada al bloquear una app.
 */
@Composable
private fun RestrictionScreen(
    uiState: RestrictionUiState,
    blockedAppName: String,
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
                text = stringResource(R.string.restriction_title),
                style = MaterialTheme.typography.headlineMedium,
                color = FocusOnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje explicativo
            Text(
                text = if (blockedAppName.isNotEmpty()) {
                    stringResource(R.string.restriction_message_named, blockedAppName)
                } else {
                    stringResource(R.string.restriction_message)
                },
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
                        text = stringResource(R.string.restriction_time_remaining),
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

            // Botón para salir de la app bloqueada hacia el inicio
            FocusPrimaryButton(
                text = stringResource(R.string.restriction_go_back),
                onClick = onGoBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón para terminar la sesión (acción destructiva)
            FocusDestructiveButton(
                text = stringResource(R.string.restriction_end_session),
                onClick = onEndSession
            )
        }
    }
}
