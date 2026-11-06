package com.focus.app.platform.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.focus.app.R
import com.focus.app.app.MainActivity
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionPhase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contenido de la notificación persistente. Solo cambia al pasar de fase, porque el sistema
 * dibuja la cuenta regresiva por sí mismo y no hace falta republicarla cada segundo.
 *
 * @param title Título de la notificación.
 * @param text Texto descriptivo.
 * @param endTimestampMs Momento en que llega a cero la cuenta regresiva.
 */
data class OngoingNotificationContent(
    val title: String,
    val text: String,
    val endTimestampMs: Long
)

/**
 * Gestor de notificaciones de Focus.
 * Crea los canales y construye la notificación persistente y el aviso de sesión completada.
 */
@Singleton
class FocusNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val ONGOING_NOTIFICATION_ID = 1001
        private const val COMPLETED_NOTIFICATION_ID = 1002
        private const val ONGOING_CHANNEL_ID = "focus_active_session_channel"
        private const val COMPLETED_CHANNEL_ID = "focus_completed_session_channel"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannels()
    }

    private fun createChannels() {
        val ongoingChannel = NotificationChannel(
            ONGOING_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        val completedChannel = NotificationChannel(
            COMPLETED_CHANNEL_ID,
            context.getString(R.string.notification_completed_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_completed_channel_description)
        }
        notificationManager.createNotificationChannels(listOf(ongoingChannel, completedChannel))
    }

    /**
     * Traduce la fase de la sesión al contenido de la notificación persistente.
     *
     * @param phase Fase actual.
     * @return El contenido, o null si en esa fase no debe haber notificación persistente.
     */
    fun contentFor(phase: SessionPhase): OngoingNotificationContent? {
        return when (phase) {
            is SessionPhase.Focusing -> OngoingNotificationContent(
                title = context.getString(
                    R.string.notification_focusing_title,
                    phase.session.activityType.displayName
                ),
                text = context.resources.getQuantityString(
                    R.plurals.notification_focusing_text,
                    phase.session.selectedPackageNames.size,
                    phase.session.selectedPackageNames.size
                ),
                endTimestampMs = phase.session.endTimestampMs
            )
            is SessionPhase.Resting -> OngoingNotificationContent(
                title = context.getString(R.string.notification_resting_title),
                text = context.getString(R.string.notification_resting_text),
                endTimestampMs = phase.breakEndTimestampMs
            )
            is SessionPhase.Completed, SessionPhase.Idle -> null
        }
    }

    /**
     * Construye la notificación persistente con cuenta regresiva nativa.
     *
     * @param content Contenido a mostrar.
     */
    fun buildOngoingNotification(content: OngoingNotificationContent): Notification {
        return NotificationCompat.Builder(context, ONGOING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setContentIntent(openAppIntent())
            .setWhen(content.endTimestampMs)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    /**
     * Construye una notificación mínima para cumplir con startForeground
     * cuando el servicio arranca sin una sesión en curso.
     */
    fun buildStartingNotification(): Notification {
        return NotificationCompat.Builder(context, ONGOING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentIntent(openAppIntent())
            .setOngoing(true)
            .build()
    }

    /**
     * Actualiza la notificación persistente ya publicada.
     *
     * @param content Nuevo contenido.
     */
    fun updateOngoingNotification(content: OngoingNotificationContent) {
        post(ONGOING_NOTIFICATION_ID, buildOngoingNotification(content))
    }

    /**
     * Avisa que la sesión terminó.
     *
     * @param session Sesión completada.
     */
    fun showCompletedNotification(session: FocusSession) {
        val notification = NotificationCompat.Builder(context, COMPLETED_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_completed_title))
            .setContentText(
                context.resources.getQuantityString(
                    R.plurals.notification_completed_text,
                    session.durationMinutes,
                    session.durationMinutes
                )
            )
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .build()
        post(COMPLETED_NOTIFICATION_ID, notification)
    }

    private fun post(id: Int, notification: Notification) {
        // Sin el permiso POST_NOTIFICATIONS la sesión funciona igual, solo sin avisos
        if (notificationManager.areNotificationsEnabled()) {
            try {
                notificationManager.notify(id, notification)
            } catch (_: SecurityException) {
                // El usuario revocó el permiso justo después de la comprobación
            }
        }
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
