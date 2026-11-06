package com.focus.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus.app.R
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentLight
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusSuccess
import com.focus.app.core.designsystem.FocusSuccessSubtle
import com.focus.app.domain.model.SmartwatchConnectionState
import com.focus.app.domain.model.SmartwatchDevice

/**
 * Tarjeta informativa del reloj Wear OS.
 * Muestra el estado real de la conexión, la batería si se conoce y el botón de escaneo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartwatchStatusCard(
    device: SmartwatchDevice,
    onTestVibration: () -> Unit,
    modifier: Modifier = Modifier,
    onScanForWatch: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = FocusCard,
        border = BorderStroke(1.dp, FocusBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Fila superior: Ícono de Reloj + Nombre del Smartwatch + Batería + Botón de escaneo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (device.isConnected) FocusSuccessSubtle else FocusAccentSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (device.isConnected) Icons.Rounded.BluetoothConnected else Icons.Rounded.Watch,
                            contentDescription = stringResource(R.string.watch_content_description),
                            tint = if (device.isConnected) FocusSuccess else FocusAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = smartwatchDisplayName(device),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = FocusOnSurface
                            )
                            if (device.isConnected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = stringResource(R.string.watch_connected_description),
                                    tint = FocusSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = smartwatchConnectionLabel(device),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (device.isConnected) FocusSuccess else FocusOnSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Píldora de batería, solo si el reloj informó el dato
                    device.batteryPercent?.let { batteryPercent ->
                        BatteryPill(batteryPercent = batteryPercent)
                    }

                    // Botón para re-escanear o buscar reloj físico
                    if (onScanForWatch != null) {
                        IconButton(
                            onClick = onScanForWatch,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.watch_scan_description),
                                tint = FocusAccentLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Chips con las protecciones que el reloj ejecuta:
            // Bloquea distracciones, Vibraciones hápticas, Detecta inactividad
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProtectionFeatureChip(
                    icon = Icons.Rounded.NotificationsOff,
                    label = stringResource(R.string.watch_feature_blocking)
                )
                ProtectionFeatureChip(
                    icon = Icons.Rounded.Vibration,
                    label = stringResource(R.string.watch_feature_haptics)
                )
                ProtectionFeatureChip(
                    icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                    label = stringResource(R.string.watch_feature_inactivity)
                )
            }

            // Barra inferior con botón para probar la vibración del reloj
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        if (device.isConnected) {
                            R.string.watch_hint_connected
                        } else {
                            R.string.watch_hint_disconnected
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = FocusOnSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = onTestVibration) {
                    Icon(
                        imageVector = Icons.Rounded.Vibration,
                        contentDescription = null,
                        tint = FocusAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.watch_test_vibration),
                        style = MaterialTheme.typography.labelMedium,
                        color = FocusAccentLight
                    )
                }
            }
        }
    }
}

/**
 * Nombre visible del reloj según su estado de conexión.
 */
@Composable
fun smartwatchDisplayName(device: SmartwatchDevice): String {
    return device.name ?: stringResource(
        if (device.connectionState == SmartwatchConnectionState.SEARCHING) {
            R.string.watch_name_searching
        } else {
            R.string.watch_name_none
        }
    )
}

/**
 * Descripción del estado de conexión del reloj.
 */
@Composable
fun smartwatchConnectionLabel(device: SmartwatchDevice): String {
    return stringResource(
        when (device.connectionState) {
            SmartwatchConnectionState.SEARCHING -> R.string.watch_state_searching
            SmartwatchConnectionState.DISCONNECTED -> R.string.watch_state_disconnected
            SmartwatchConnectionState.MISSING_WEAR_APP -> R.string.watch_state_missing_app
            SmartwatchConnectionState.CONNECTED -> {
                if (device.isNearby) R.string.watch_state_bluetooth else R.string.watch_state_cloud
            }
        }
    )
}

@Composable
private fun BatteryPill(batteryPercent: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FocusSuccessSubtle,
        border = BorderStroke(1.dp, FocusSuccess.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.BatteryFull,
                contentDescription = stringResource(R.string.watch_battery_description),
                tint = FocusSuccess,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "$batteryPercent%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = FocusSuccess
            )
        }
    }
}

@Composable
private fun ProtectionFeatureChip(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = FocusAccentSubtle.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, FocusBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FocusAccentLight,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = FocusOnSurface
            )
        }
    }
}
