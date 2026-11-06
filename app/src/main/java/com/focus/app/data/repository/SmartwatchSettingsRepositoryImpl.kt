package com.focus.app.data.repository

import com.focus.app.data.local.preferences.FocusPreferencesDataSource
import com.focus.app.domain.model.SmartwatchSettings
import com.focus.app.domain.repository.SmartwatchSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de las preferencias del reloj sobre DataStore.
 */
@Singleton
class SmartwatchSettingsRepositoryImpl @Inject constructor(
    private val dataSource: FocusPreferencesDataSource
) : SmartwatchSettingsRepository {

    override val settings: Flow<SmartwatchSettings> = dataSource.smartwatchSettings

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataSource.setSmartwatchHapticsEnabled(enabled)
    }

    override suspend fun setInactivityAlertsEnabled(enabled: Boolean) {
        dataSource.setSmartwatchInactivityEnabled(enabled)
    }

    override suspend fun setInactivityThresholdMinutes(minutes: Int) {
        dataSource.setSmartwatchInactivityThreshold(minutes)
    }

    override suspend fun setRestIndicatorEnabled(enabled: Boolean) {
        dataSource.setSmartwatchRestIndicatorEnabled(enabled)
    }
}
