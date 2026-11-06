package com.focus.app.feature.settings

import com.focus.app.domain.model.SmartwatchDevice
import com.focus.app.domain.model.SmartwatchSettings

/**
 * Estado inmutable de la UI para la pantalla de configuración.
 *
 * @param isAccessibilityServiceEnabled Si el servicio de accesibilidad está habilitado en Android.
 * @param preferredDurationMinutes Duración por defecto seleccionada por el usuario (en minutos).
 * @param appVersion Versión de la aplicación instalada.
 * @param smartwatchSettings Preferencias del reloj.
 * @param smartwatchDevice Estado real del reloj.
 */
data class SettingsUiState(
    val isAccessibilityServiceEnabled: Boolean = false,
    val preferredDurationMinutes: Int = 45,
    val appVersion: String = "",
    val smartwatchSettings: SmartwatchSettings = SmartwatchSettings(),
    val smartwatchDevice: SmartwatchDevice = SmartwatchDevice()
)
