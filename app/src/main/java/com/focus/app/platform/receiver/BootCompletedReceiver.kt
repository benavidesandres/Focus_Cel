package com.focus.app.platform.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focus.app.domain.repository.FocusSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receptor de broadcast para BOOT_COMPLETED.
 * Reconcilia el estado de sesión persistida tras un reinicio del dispositivo.
 *
 * Si una sesión estaba activa cuando el dispositivo se reinició:
 * 1. Carga la sesión persistida de DataStore.
 * 2. Verifica si el timestamp de fin ya pasó.
 * 3. Si expiró, limpia el estado para evitar que la próxima apertura de
 *    la app restaure una sesión ya vencida.
 *
 * Nota: El receptor no puede garantizar que las restricciones de accesibilidad
 * sigan activas tras el reinicio, ya que el servicio también debe reiniciarse.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionRepository: FocusSessionRepository

    // Ámbito de coroutine para operaciones asíncronas del receptor
    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        // Solo procesa eventos de inicio del sistema
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) return

        reconcileSessionState()
    }

    /**
     * Reconcilia el estado de la sesión almacenada con el tiempo actual.
     * Si la sesión expiró durante el reinicio, la limpia.
     */
    private fun reconcileSessionState() {
        receiverScope.launch {
            val session = sessionRepository.loadPersistedSession() ?: return@launch
            val nowMs = System.currentTimeMillis()

            if (session.isExpired(nowMs)) {
                // La sesión expiró durante el reinicio; se limpia para evitar
                // que la app muestre un estado de sesión activa desactualizado
                sessionRepository.clearActiveSession()
            }
            // Si la sesión aún no expiró, se deja persistida para que la app
            // la restaure cuando el usuario la abra
        }
    }
}
