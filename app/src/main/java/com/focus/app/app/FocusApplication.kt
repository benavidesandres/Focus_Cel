package com.focus.app.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase de aplicación principal de Focus.
 * La anotación @HiltAndroidApp activa la generación de código de Hilt
 * e inicializa el grafo de dependencias al arrancar la aplicación.
 */
@HiltAndroidApp
class FocusApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicialización de componentes globales puede añadirse aquí si se requiere
    }
}
