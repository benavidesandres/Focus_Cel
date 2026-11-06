package com.focus.app.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.BuildConfig
import com.focus.app.domain.repository.SmartwatchGateway
import com.focus.app.domain.repository.SmartwatchSettingsRepository
import com.focus.app.domain.usecase.GetRestrictionStatusUseCase
import com.focus.app.platform.accessibility.FocusAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de configuración de Focus.
 *
 * Administra las preferencias globales, monitorea el estado del servicio de accesibilidad
 * y gestiona las preferencias del reloj.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restrictionStatus: GetRestrictionStatusUseCase,
    private val settingsRepository: SmartwatchSettingsRepository,
    private val smartwatch: SmartwatchGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(appVersion = BuildConfig.VERSION_NAME))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            restrictionStatus.preferredDurationMinutes.collect { minutes ->
                _uiState.update { it.copy(preferredDurationMinutes = minutes) }
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { it.copy(smartwatchSettings = settings) }
            }
        }
        viewModelScope.launch {
            smartwatch.device.collect { device ->
                _uiState.update { it.copy(smartwatchDevice = device) }
            }
        }
    }

    fun checkAccessibilityService() {
        val isEnabled = FocusAccessibilityService.isServiceEnabled(context)
        _uiState.update { it.copy(isAccessibilityServiceEnabled = isEnabled) }
    }

    fun onDurationSelected(minutes: Int) {
        viewModelScope.launch { restrictionStatus.savePreferredDuration(minutes) }
    }

    fun onToggleHaptics(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun onToggleInactivityAlerts(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setInactivityAlertsEnabled(enabled) }
    }

    fun onInactivityThresholdSelected(minutes: Int) {
        viewModelScope.launch { settingsRepository.setInactivityThresholdMinutes(minutes) }
    }

    fun onToggleRestIndicator(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setRestIndicatorEnabled(enabled) }
    }
}
