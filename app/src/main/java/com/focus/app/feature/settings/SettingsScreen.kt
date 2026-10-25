package com.focus.app.feature.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
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
import com.focus.app.core.designsystem.FocusWarning
import com.focus.app.core.designsystem.FocusWarningSubtle
import com.focus.app.core.designsystem.components.FocusDurationSelector
import com.focus.app.core.designsystem.components.FocusSecondaryButton
import com.focus.app.platform.accessibility.FocusAccessibilityService

/**
 * Pantalla de configuración de la aplicación Focus.
 *
 * Permite verificar y gestionar el estado del servicio de accesibilidad, configurar
 * la duración por defecto de las sesiones de enfoque y consultar información de privacidad.
 *
 * @param onNavigateBack Acción de retroceso hacia la pantalla principal.
 * @param viewModel ViewModel inyectado por Hilt.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Actualiza el estado del servicio de accesibilidad al regresar a la pantalla
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkAccessibilityService()
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
        ) {
            // ── Barra superior ───────────────────────────────────────────────
            SettingsTopBar(onNavigateBack = onNavigateBack)

            // ── Contenido con desplazamiento ─────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ── Sección 1: Servicio de accesibilidad ──────────────────────
                AccessibilitySection(
                    isEnabled = uiState.isAccessibilityServiceEnabled,
                    onOpenSettings = {
                        context.startActivity(
                            FocusAccessibilityService.openAccessibilitySettingsIntent()
                        )
                    }
                )

                // ── Sección 2: Preferencias de duración ───────────────────────
                PreferencesSection(
                    currentDuration = uiState.preferredDurationMinutes,
                    onDurationSelected = { viewModel.onDurationSelected(it) }
                )

                // ── Sección 3: Privacidad y detalles de la aplicación ──────────
                AboutSection(versionName = uiState.appVersion)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Encabezado de la pantalla de configuración con botón de navegación hacia atrás.
 */
@Composable
private fun SettingsTopBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = FocusOnSurface
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleMedium,
            color = FocusOnSurface
        )
    }
}

/**
 * Sección de estado y configuración del Servicio de Accesibilidad.
 */
@Composable
private fun AccessibilitySection(
    isEnabled: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = if (isEnabled) FocusSuccess else FocusWarning,
        animationSpec = spring(stiffness = 400f),
        label = "AccessibilityStatusColor"
    )

    val statusBgColor by animateColorAsState(
        targetValue = if (isEnabled) FocusSuccessSubtle else FocusWarningSubtle,
        animationSpec = spring(stiffness = 400f),
        label = "AccessibilityStatusBg"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_accessibility_section),
            style = MaterialTheme.typography.titleSmall,
            color = FocusAccent
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = FocusCard,
            border = BorderStroke(1.dp, FocusBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Estado del servicio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEnabled) {
                                stringResource(R.string.settings_accessibility_enabled)
                            } else {
                                stringResource(R.string.settings_accessibility_disabled)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = FocusOnSurface
                        )
                    }
                }

                // Descripción de propósito y privacidad
                Text(
                    text = stringResource(R.string.settings_accessibility_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusOnSurfaceVariant
                )

                // Botón para abrir ajustes del sistema
                FocusSecondaryButton(
                    text = stringResource(R.string.settings_open_accessibility),
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Sección de preferencias de duración por defecto.
 */
@Composable
private fun PreferencesSection(
    currentDuration: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_preferences_section),
            style = MaterialTheme.typography.titleSmall,
            color = FocusAccent
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = FocusCard,
            border = BorderStroke(1.dp, FocusBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_default_duration),
                    style = MaterialTheme.typography.titleSmall,
                    color = FocusOnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sessions will start with this duration by default.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                FocusDurationSelector(
                    selectedDurationMinutes = currentDuration,
                    onDurationSelected = onDurationSelected
                )
            }
        }
    }
}

/**
 * Sección de privacidad e información de la aplicación.
 */
@Composable
private fun AboutSection(
    versionName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_about_section),
            style = MaterialTheme.typography.titleSmall,
            color = FocusAccent
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = FocusCard,
            border = BorderStroke(1.dp, FocusBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fila de privacidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FocusAccentSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PrivacyTip,
                            contentDescription = null,
                            tint = FocusAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy Guarantee",
                            style = MaterialTheme.typography.titleSmall,
                            color = FocusOnSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_privacy),
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusOnSurfaceVariant
                        )
                    }
                }

                // Fila de versión
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusOnSurfaceVariant
                    )
                    Text(
                        text = versionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusOnSurface
                    )
                }
            }
        }
    }
}
