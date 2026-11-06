package com.focus.app.platform.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receptor de BOOT_COMPLETED.
 *
 * Su única función es que el sistema cree el proceso tras el reinicio: FocusApplication
 * inicializa entonces el gestor de sesiones, que restaura o cierra la sesión guardada.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = Unit
}
