package com.focus.app.app

import android.app.Application
import com.focus.app.domain.session.FocusSessionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Clase de aplicación principal de Focus.
 * La anotación @HiltAndroidApp activa la generación de código de Hilt
 * e inicializa el grafo de dependencias al arrancar la aplicación.
 *
 * Cualquier componente puede crear el proceso (el servicio de accesibilidad, un mensaje del
 * reloj o el reinicio del dispositivo), por lo que la sesión se restaura aquí y no en una pantalla.
 */
@HiltAndroidApp
class FocusApplication : Application() {

    @Inject
    lateinit var sessionManager: FocusSessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager.initialize()
    }
}
