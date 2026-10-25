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
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.focus.app.R
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
import com.focus.app.core.designsystem.components.FocusDurationSelector
import com.focus.app.core.designsystem.components.FocusSecondaryButton
import com.focus.app.platform.accessibility.FocusAccessibilityService

/**
 * Pantalla de configuración de Focus adaptada con las opciones del Smartwatch Companion.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
            SettingsTopBar(onNavigateBack = onNavigateBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ── Sección 1: Configuración del Smartwatch ───────────────────
                SmartwatchSettingsSection(
                    uiState = uiState,
                    onToggleHaptic = viewModel::onToggleSmartwatchHaptic,
                    onToggleSedentarism = viewModel::onToggleSmartwatchSedentarism,
                    onSelectThreshold = viewModel::onSedentarismThresholdSelected,
                    onToggleRestIndicator = viewModel::onToggleSmartwatchRestIndicator
                )

                // ── Sección 2: Duración predeterminada ────────────────────────
                PreferencesSection(
                    currentDuration = uiState.preferredDurationMinutes,
                    onDurationSelected = viewModel::onDurationSelected
                )

                // ── Sección 3: Servicio de accesibilidad ──────────────────────
                AccessibilitySection(
                    isEnabled = uiState.isAccessibilityServiceEnabled,
                    onOpenSettings = {
                        context.startActivity(
                            FocusAccessibilityService.openAccessibilitySettingsIntent()
                        )
                    }
                )

                // ── Sección 4: Información y privacidad ───────────────────────
                AboutSection(versionName = uiState.appVersion)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

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
            text = "Configuración y Reloj",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = FocusOnSurface
        )
    }
}

/**
 * Sección de opciones del reloj inteligente (Vibraciones, Sedentarismo, Descanso).
 */
@Composable
private fun SmartwatchSettingsSection(
    uiState: SettingsUiState,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleSedentarism: (Boolean) -> Unit,
    onSelectThreshold: (Int) -> Unit,
    onToggleRestIndicator: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Smartwatch Focus (Wear OS)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Estado del reloj conectado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FocusAccentSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Watch,
                            contentDescription = null,
                            tint = FocusAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.smartwatchName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = FocusOnSurface
                        )
                        Text(
                            text = if (uiState.isSmartwatchConnected) "Sincronizado vía Wear OS Data Layer" else "Buscando reloj...",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusOnSurfaceVariant
                        )
                    }
                }

                // Opción: Vibraciones táctiles en muñeca
                SettingSwitchRow(
                    icon = Icons.Rounded.Vibration,
                    title = "Vibraciones hápticas en reloj",
                    description = "Vibra al iniciar, a la mitad y al terminar para que no tengas que mirar la pantalla.",
                    checked = uiState.isSmartwatchHapticEnabled,
                    onCheckedChange = onToggleHaptic
                )

                // Opción: Detección de inactividad física
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingSwitchRow(
                        icon = Icons.Rounded.DirectionsWalk,
                        title = "Detección de inactividad",
                        description = "Detecta que llevas mucho tiempo sin moverte y te avisa para estirarte.",
                        checked = uiState.isSmartwatchSedentarismEnabled,
                        onCheckedChange = onToggleSedentarism
                    )

                    if (uiState.isSmartwatchSedentarismEnabled) {
                        Text(
                            text = "Avisar tras inactividad continua:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = FocusOnSurfaceVariant,
                            modifier = Modifier.padding(start = 44.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 44.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 45, 60).forEach { mins ->
                                val isSelected = uiState.smartwatchSedentarismThresholdMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectThreshold(mins) },
                                    label = { Text("$mins min", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FocusAccentSubtle,
                                        selectedLabelColor = FocusAccentLight
                                    )
                                )
                            }
                        }
                    }
                }

                // Opción: Indicador de descanso
                SettingSwitchRow(
                    icon = Icons.Rounded.SelfImprovement,
                    title = "Indicador de descanso guiado",
                    description = "Al terminar los 45 minutos, el reloj vibra e indica el momento exacto de descansar.",
                    checked = uiState.isSmartwatchRestIndicatorEnabled,
                    onCheckedChange = onToggleRestIndicator
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(FocusAccentSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FocusAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = FocusOnSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = FocusOnSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FocusAccentLight,
                checkedTrackColor = FocusAccent
            )
        )
    }
}

@Composable
private fun PreferencesSection(
    currentDuration: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Duración Predeterminada",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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
                    text = "Tiempo de concentración habitual",
                    style = MaterialTheme.typography.titleSmall,
                    color = FocusOnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "El bloque estándar recomendado para el reloj es de 45 minutos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                FocusDurationSelector(
                    selectedMinutes = currentDuration,
                    onOptionSelected = onDurationSelected
                )
            }
        }
    }
}

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
            text = "Bloqueo en el Celular",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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
                            text = if (isEnabled) "Servicio de bloqueo activo" else "Servicio de bloqueo inactivo",
                            style = MaterialTheme.typography.titleSmall,
                            color = FocusOnSurface
                        )
                    }
                }

                Text(
                    text = "El servicio detecta apps distractoras en el celular mientras el reloj está en modo concentración.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusOnSurfaceVariant
                )

                FocusSecondaryButton(
                    text = "Abrir Ajustes de Accesibilidad",
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AboutSection(
    versionName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Privacidad y Versión",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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
                            text = "Privacidad sin Conexión Externa",
                            style = MaterialTheme.typography.titleSmall,
                            color = FocusOnSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Focus procesa todo de forma local entre tu celular y tu reloj inteligente. No se envían datos personales ni estadísticas a servidores externos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusOnSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Versión Companion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusOnSurfaceVariant
                    )
                    Text(
                        text = "$versionName (Smartwatch Edition)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusOnSurface
                    )
                }
            }
        }
    }
}
