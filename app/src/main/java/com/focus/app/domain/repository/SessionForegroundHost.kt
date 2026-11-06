package com.focus.app.domain.repository

import com.focus.app.domain.model.FocusSession

/**
 * Abstrae la presencia de la sesión en el sistema operativo.
 * Permite que el gestor de sesiones no dependa de las clases de Android.
 */
interface SessionForegroundHost {

    /** Mantiene vivo el proceso con la notificación persistente mientras dure la sesión. */
    fun start()

    /** Informa al usuario que la sesión terminó, aunque la app esté cerrada. */
    fun notifyCompleted(session: FocusSession)
}
