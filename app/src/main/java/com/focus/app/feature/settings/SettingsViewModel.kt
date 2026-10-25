package com.focus.app.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.BuildConfig
import com.focus.app.data.local.preferences.FocusPreferencesDataSource
import com.focus.app.domain.usecase.GetRestrictionStatusUseCase
import com.focus.app.domain.usecase.SaveSelectedAppsUseCase
import com.focus.app.platform.accessibility.FocusAccessibilityService
import com.focus.app.platform.smartwatch.SmartwatchSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de configuración de Focus.
 *
 * Administra las preferencias globales, monitorea el estado del servicio de accesibilidad
 * y gestiona las configuraciones de sincronización háptica y detección de sedentarismo del Smartwatch.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restrictionStatus: GetRestrictionStatusUseCase,
    private val saveSelectedApps: SaveSelectedAppsUseCase,
    private val preferencesDataSource: FocusPreferencesDataSource,
    val smartwatchSyncManager: SmartwatchSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appVersion = BuildConfig.VERSION_NAME
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        observeSmartwatch()
        checkAccessibilityService()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                restrictionStatus.preferredDurationMinutes,
                saveSelectedApps.selectedPackageNames,
                preferencesDataSource.smartwatchHapticEnabled,
                preferencesDataSource.smartwatchSedentarismEnabled,
                preferencesDataSource.smartwatchSedentarismThresholdMinutes,
                preferencesDataSource.smartwatchRestIndicatorEnabled
            ) { duration, packages, haptic, sedentarism, threshold, restIndicator ->
                SettingsData(
                    duration = if (duration > 0) duration else 45,
                    packageCount = packages.size,
                    haptic = haptic,
                    sedentarism = sedentarism,
                    threshold = threshold,
                    restIndicator = restIndicator
                )
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        preferredDurationMinutes = data.duration,
                        selectedAppsCount = data.packageCount,
                        isSmartwatchHapticEnabled = data.haptic,
                        isSmartwatchSedentarismEnabled = data.sedentarism,
                        smartwatchSedentarismThresholdMinutes = data.threshold,
                        isSmartwatchRestIndicatorEnabled = data.restIndicator
                    )
                }
            }
        }
    }

    private fun observeSmartwatch() {
        viewModelScope.launch {
            smartwatchSyncManager.connectedWatch.collect { watch ->
                _uiState.update {
                    it.copy(
                        smartwatchName = watch.name,
                        isSmartwatchConnected = watch.isConnected
                    )
                }
            }
        }
    }

    fun checkAccessibilityService() {
        val isEnabled = FocusAccessibilityService.isServiceEnabled(context)
        _uiState.update { it.copy(isAccessibilityServiceEnabled = isEnabled) }
    }

    fun onDurationSelected(minutes: Int) {
        viewModelScope.launch {
            restrictionStatus.savePreferredDuration(minutes)
        }
    }

    fun onToggleSmartwatchHaptic(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.setSmartwatchHapticEnabled(enabled)
        }
    }

    fun onToggleSmartwatchSedentarism(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.setSmartwatchSedentarismEnabled(enabled)
        }
    }

    fun onSedentarismThresholdSelected(minutes: Int) {
        viewModelScope.launch {
            preferencesDataSource.setSmartwatchSedentarismThreshold(minutes)
        }
    }

    fun onToggleSmartwatchRestIndicator(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.setSmartwatchRestIndicatorEnabled(enabled)
        }
    }

    private data class SettingsData(
        val duration: Int,
        val packageCount: Int,
        val haptic: Boolean,
        val sedentarism: Boolean,
        val threshold: Int,
        val restIndicator: Boolean
    )
}
