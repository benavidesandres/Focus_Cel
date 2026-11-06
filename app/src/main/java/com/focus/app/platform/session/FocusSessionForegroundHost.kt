package com.focus.app.platform.session

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.repository.SessionForegroundHost
import com.focus.app.platform.notification.FocusNotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de [SessionForegroundHost] basada en [FocusSessionService].
 */
@Singleton
class FocusSessionForegroundHost @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHelper: FocusNotificationHelper
) : SessionForegroundHost {

    override fun start() {
        try {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FocusSessionService::class.java)
            )
        } catch (e: IllegalStateException) {
            // Android puede rechazarlo desde segundo plano (por ejemplo, una orden del reloj
            // sin el servicio de accesibilidad activo). La sesión sigue funcionando sin él.
            Log.w(TAG, "Could not start the foreground service", e)
        }
    }

    override fun notifyCompleted(session: FocusSession) {
        notificationHelper.showCompletedNotification(session)
    }

    private companion object {
        const val TAG = "FocusForegroundHost"
    }
}
