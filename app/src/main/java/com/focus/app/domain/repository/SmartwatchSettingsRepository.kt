package com.focus.app.domain.repository

import com.focus.app.domain.model.SmartwatchSettings
import kotlinx.coroutines.flow.Flow

/**
 * Persistencia de las preferencias del reloj.
 */
interface SmartwatchSettingsRepository {

    /** Flujo observable de las preferencias actuales. */
    val settings: Flow<SmartwatchSettings>

    suspend fun setHapticsEnabled(enabled: Boolean)

    suspend fun setInactivityAlertsEnabled(enabled: Boolean)

    suspend fun setInactivityThresholdMinutes(minutes: Int)

    suspend fun setRestIndicatorEnabled(enabled: Boolean)
}
