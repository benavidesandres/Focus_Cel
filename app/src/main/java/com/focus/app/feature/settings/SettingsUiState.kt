package com.focus.app.feature.settings

/**
 * Estado inmutable de la UI para la pantalla de configuración.
 *
 * @param isAccessibilityServiceEnabled Si el servicio de accesibilidad está habilitado en Android.
 * @param preferredDurationMinutes Duración por defecto seleccionada por el usuario (en minutos).
 * @param selectedAppsCount Número de aplicaciones configuradas actualmente para restricción.
 * @param appVersion Versión de la aplicación instalada.
 */
data class SettingsUiState(
    val isAccessibilityServiceEnabled: Boolean = false,
    val preferredDurationMinutes: Int = 25,
    val selectedAppsCount: Int = 0,
    val appVersion: String = "1.0.0"
)
