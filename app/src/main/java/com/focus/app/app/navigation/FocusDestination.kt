package com.focus.app.app.navigation

/**
 * Destinos de navegación tipados para la app Focus.
 * Cada destino encapsula su ruta y argumentos opcionales.
 */
sealed class FocusDestination(val route: String) {

    /** Pantalla principal con el resumen de configuración de sesión. */
    data object Home : FocusDestination("home")

    /** Pantalla de selección de aplicaciones a restringir. */
    data object AppSelection : FocusDestination("app_selection")

    /** Pantalla de sesión activa con el temporizador de cuenta regresiva. */
    data object ActiveSession : FocusDestination("active_session")

    /** Pantalla de configuración de la aplicación. */
    data object Settings : FocusDestination("settings")
}
