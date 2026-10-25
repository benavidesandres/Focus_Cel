package com.focus.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentBorder
import com.focus.app.core.designsystem.FocusAccentLight
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusSuccess
import com.focus.app.core.designsystem.FocusWarning
import com.focus.app.domain.model.FocusActivityType

/**
 * Vista previa interactiva de la pantalla ultra-simple del Smartwatch.
 *
 * Muestra el diseño minimalista de esfera redonda OLED que el reloj inteligente
 * muestra en la muñeca del usuario para proteger su atención sin saturarlo de información.
 */
@Composable
fun SmartwatchLivePreview(
    activity: FocusActivityType,
    timeLabel: String,
    progress: Float = 0f,
    isSedentaryWarning: Boolean = false,
    isRestMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = FocusCard,
        border = BorderStroke(1.dp, FocusBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pantalla del Smartwatch Focus",
                    style = MaterialTheme.typography.titleSmall,
                    color = FocusOnSurface
                )
                Text(
                    text = "Modo Ultra-Simple",
                    style = MaterialTheme.typography.labelSmall,
                    color = FocusAccentLight
                )
            }

            // Bisel redondo simulado del smartwatch
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F1117))
                    .border(
                        BorderStroke(
                            4.dp,
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF2E3245),
                                    Color(0xFF1E2130),
                                    Color(0xFF3B4058),
                                    Color(0xFF2E3245)
                                )
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Anillo de progreso de la esfera
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = tween(600),
                    label = "WatchRingProgress"
                )

                val ringColor by animateColorAsState(
                    targetValue = when {
                        isRestMode -> FocusSuccess
                        isSedentaryWarning -> FocusWarning
                        else -> FocusAccent
                    },
                    label = "WatchRingColor"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Arco de fondo
                    drawArc(
                        color = Color(0xFF1C1F2B),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Arco de progreso activo
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Contenido central de la esfera del reloj (ultra-minimalista)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Actividad o modo
                    Text(
                        text = when {
                            isRestMode -> "DESCANSO"
                            isSedentaryWarning -> "¡MUÉVETE!"
                            else -> activity.displayName.uppercase()
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = when {
                            isRestMode -> FocusSuccess
                            isSedentaryWarning -> FocusWarning
                            else -> FocusAccentLight
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tiempo de alta legibilidad
                    Text(
                        text = timeLabel,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Indicadores del reloj: Sin distracciones + Háptico
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRestMode) Icons.Rounded.SelfImprovement else Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = when {
                                isRestMode -> "Respira hondo"
                                isSedentaryWarning -> "Ponte de pie"
                                else -> "Cero alertas"
                            },
                            fontSize = 9.sp,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Explicación de valor bajo la esfera
            Text(
                text = "Tu atención se protege: la pantalla permanece oscura y el reloj te avisa por vibración háptica cuándo descansar o moverte.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = FocusOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
