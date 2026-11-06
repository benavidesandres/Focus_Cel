package com.focus.app.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.SessionPhase
import com.focus.app.domain.repository.SmartwatchGateway
import com.focus.app.domain.session.FocusSessionManager
import com.focus.app.domain.usecase.GetRestrictionStatusUseCase
import com.focus.app.domain.usecase.SaveSelectedAppsUseCase
import com.focus.app.platform.accessibility.FocusAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla principal de Focus.
 * Coordina la actividad, la duración, las apps seleccionadas y el estado del reloj.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: FocusSessionManager,
    private val saveSelectedApps: SaveSelectedAppsUseCase,
    private val restrictionStatus: GetRestrictionStatusUseCase,
    private val smartwatch: SmartwatchGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSelectedApps()
        observePreferredDuration()
        observeSessionPhase()
        observeSmartwatch()
    }

    private fun observeSelectedApps() {
        viewModelScope.launch {
            saveSelectedApps.selectedPackageNames.collect { packages ->
                _uiState.update { it.copy(selectedAppCount = packages.size) }
            }
        }
    }

    private fun observePreferredDuration() {
        viewModelScope.launch {
            restrictionStatus.preferredDurationMinutes.collect { minutes ->
                _uiState.update { it.copy(selectedDurationMinutes = minutes) }
            }
        }
    }

    private fun observeSessionPhase() {
        viewModelScope.launch {
            sessionManager.phase
                .map { it != SessionPhase.Idle }
                .distinctUntilChanged()
                .collect { hasOngoingSession ->
                    _uiState.update { it.copy(hasOngoingSession = hasOngoingSession) }
                }
        }
    }

    private fun observeSmartwatch() {
        viewModelScope.launch {
            smartwatch.device.collect { device ->
                _uiState.update { it.copy(smartwatchDevice = device) }
            }
        }
    }

    /** Actualiza lo que pudo cambiar fuera de la app mientras la pantalla no estaba visible. */
    fun onScreenResumed() {
        val isEnabled = FocusAccessibilityService.isServiceEnabled(context)
        _uiState.update { it.copy(isAccessibilityServiceEnabled = isEnabled) }
        smartwatch.refreshConnection()
    }

    fun onActivitySelected(activity: FocusActivityType) {
        _uiState.update { it.copy(selectedActivity = activity) }
    }

    fun onDurationSelected(minutes: Int) {
        viewModelScope.launch {
            restrictionStatus.savePreferredDuration(minutes)
        }
    }

    fun onTestSmartwatchVibration() {
        smartwatch.testVibration()
    }

    fun onScanForWatch() {
        smartwatch.refreshConnection()
    }

    /** Inicia la sesión; la pantalla navega al detectar el cambio de fase. */
    fun onStartSession() {
        viewModelScope.launch {
            val state = _uiState.value
            sessionManager.start(state.selectedDurationMinutes, state.selectedActivity)
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
