package com.focus.app.feature.settings

/**
 * Estado inmutable de la UI para la pantalla de configuración con Smartwatch Companion.
 *
 * @param isAccessibilityServiceEnabled Si el servicio de accesibilidad está habilitado en Android.
 * @param preferredDurationMinutes Duración por defecto seleccionada por el usuario (en minutos).
 * @param selectedAppsCount Número de aplicaciones configuradas actualmente para restricción.
 * @param appVersion Versión de la aplicación instalada.
 * @param isSmartwatchHapticEnabled Si las vibraciones hápticas en la muñeca están activadas.
 * @param isSmartwatchSedentarismEnabled Si la detección de inactividad física prolongada está activa.
 * @param smartwatchSedentarismThresholdMinutes Umbral de inactividad antes de vibrar (ej. 45 min).
 * @param isSmartwatchRestIndicatorEnabled Si el reloj indica automáticamente cuándo descansar al terminar.
 * @param smartwatchName Nombre del reloj inteligente conectado.
 * @param isSmartwatchConnected Si hay comunicación con el smartwatch.
 */
data class SettingsUiState(
    val isAccessibilityServiceEnabled: Boolean = false,
    val preferredDurationMinutes: Int = 45,
    val selectedAppsCount: Int = 0,
    val appVersion: String = "1.0.0",
    val isSmartwatchHapticEnabled: Boolean = true,
    val isSmartwatchSedentarismEnabled: Boolean = true,
    val smartwatchSedentarismThresholdMinutes: Int = 45,
    val isSmartwatchRestIndicatorEnabled: Boolean = true,
    val smartwatchName: String = "Focus Smartwatch",
    val isSmartwatchConnected: Boolean = true
)
