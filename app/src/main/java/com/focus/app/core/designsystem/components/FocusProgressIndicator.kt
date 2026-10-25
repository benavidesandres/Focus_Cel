package com.focus.app.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.FocusProgressActive
import com.focus.app.core.designsystem.FocusProgressTrack

/**
 * Indicador de progreso circular para la pantalla de sesión activa.
 * Muestra el tiempo restante como arco sobre una pista circular.
 *
 * @param progress Fracción del progreso (0f = sin inicio, 1f = completado).
 * @param timeRemainingLabel Texto formateado del tiempo restante (ej: "24:59").
 * @param statusLabel Etiqueta de estado debajo del tiempo.
 * @param strokeWidth Grosor del arco de progreso.
 * @param modifier Modificador externo.
 */
@Composable
fun FocusProgressIndicator(
    progress: Float,
    timeRemainingLabel: String,
    statusLabel: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    trackColor: Color = FocusProgressTrack,
    progressColor: Color = FocusProgressActive
) {
    // Animación suave del progreso para evitar saltos bruscos
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 60f),
        label = "FocusProgressIndicator_progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        // Arco de progreso circular dibujado con Canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(inset, inset)

            // Pista de fondo
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Arco de progreso activo (barrido desde arriba en sentido horario)
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * (1f - animatedProgress),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Contenido central: tiempo restante y etiqueta
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tiempo restante con transición animada al cambiar
            AnimatedContent(
                targetState = timeRemainingLabel,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                },
                label = "FocusProgressIndicator_time"
            ) { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.displayMedium,
                    color = FocusOnSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = FocusOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
