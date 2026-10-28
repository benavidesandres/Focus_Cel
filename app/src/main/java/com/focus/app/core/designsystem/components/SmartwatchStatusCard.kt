package com.focus.app.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentLight
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusSuccess
import com.focus.app.core.designsystem.FocusSuccessSubtle
import com.focus.app.domain.model.SmartwatchDevice

/**
 * Tarjeta informativa del Smartwatch conectado (Wear OS Companion).
 * Muestra el estado del reloj real, nivel de batería, protecciones activas y botón de escaneo.
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
                            .background(if (device.isRealHardware) FocusSuccessSubtle else FocusAccentSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (device.isRealHardware) Icons.Rounded.BluetoothConnected else Icons.Rounded.Watch,
                            contentDescription = "Smartwatch",
                            tint = if (device.isRealHardware) FocusSuccess else FocusAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = FocusOnSurface
                            )
                            if (device.isConnected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Conectado",
                                    tint = FocusSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = device.connectionLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (device.isRealHardware) FocusSuccess else FocusOnSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Píldora de batería
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
                                contentDescription = "Batería",
                                tint = FocusSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = device.batteryStatusLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FocusSuccess
                            )
                        }
                    }

                    // Botón para re-escanear o buscar reloj físico
                    if (onScanForWatch != null) {
                        IconButton(
                            onClick = onScanForWatch,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Buscar reloj real",
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
                    label = "Bloquea distracciones"
                )
                ProtectionFeatureChip(
                    icon = Icons.Rounded.Vibration,
                    label = "Vibraciones hápticas"
                )
                ProtectionFeatureChip(
                    icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                    label = "Detecta inactividad"
                )
            }

            // Barra inferior con botón para probar la vibración del reloj
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (device.isRealHardware) {
                        "Reloj real conectado. Las vibraciones y alertas se envían a tu muñeca."
                    } else {
                        "El reloj vibrará al iniciar, descansar y si no te mueves."
                    },
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
                        text = "Probar reloj",
                        style = MaterialTheme.typography.labelMedium,
                        color = FocusAccentLight
                    )
                }
            }
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
