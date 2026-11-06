package com.focus.app.platform.session

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.focus.app.domain.session.FocusSessionManager
import com.focus.app.platform.notification.FocusNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Servicio en primer plano que mantiene vivo el proceso durante la sesión y el descanso.
 *
 * No contiene lógica de sesión: solo refleja en la notificación la fase publicada por
 * [FocusSessionManager] y se detiene cuando ya no hay nada en curso.
 */
@AndroidEntryPoint
class FocusSessionService : Service() {

    @Inject
    lateinit var sessionManager: FocusSessionManager

    @Inject
    lateinit var notificationHelper: FocusNotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var phaseObserver: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // El sistema exige llamar a startForeground aunque la sesión ya haya terminado
        val content = notificationHelper.contentFor(sessionManager.phase.value)
        ServiceCompat.startForeground(
            this,
            FocusNotificationHelper.ONGOING_NOTIFICATION_ID,
            content?.let(notificationHelper::buildOngoingNotification)
                ?: notificationHelper.buildStartingNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        if (phaseObserver == null) phaseObserver = observePhase()

        // Si el sistema mata el proceso, lo recrea y la aplicación restaura la sesión
        return START_STICKY
    }

    private fun observePhase(): Job = serviceScope.launch {
        sessionManager.phase
            .map(notificationHelper::contentFor)
            .distinctUntilChanged()
            .collect { content ->
                if (content == null) {
                    ServiceCompat.stopForeground(
                        this@FocusSessionService,
                        ServiceCompat.STOP_FOREGROUND_REMOVE
                    )
                    stopSelf()
                } else {
                    notificationHelper.updateOngoingNotification(content)
                }
            }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
