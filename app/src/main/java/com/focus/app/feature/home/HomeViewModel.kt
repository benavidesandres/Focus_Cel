package com.focus.app.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.usecase.GetRestrictionStatusUseCase
import com.focus.app.domain.usecase.ObserveFocusSessionUseCase
import com.focus.app.domain.usecase.SaveSelectedAppsUseCase
import com.focus.app.domain.usecase.StartFocusSessionUseCase
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
 * ViewModel de la pantalla principal de Focus adaptada para Smartwatch Companion.
 * Coordina la actividad "¿Qué vas a hacer?", la duración, las apps y la sincronización con el reloj.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeSession: ObserveFocusSessionUseCase,
    private val startSession: StartFocusSessionUseCase,
    private val saveSelectedApps: SaveSelectedAppsUseCase,
    private val restrictionStatus: GetRestrictionStatusUseCase,
    val smartwatchSyncManager: SmartwatchSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigateToSession = MutableStateFlow(false)
    val navigateToSession: StateFlow<Boolean> = _navigateToSession.asStateFlow()

    init {
        observeState()
        observeSmartwatch()
        reconcileSession()
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                saveSelectedApps.selectedPackageNames,
                restrictionStatus.preferredDurationMinutes,
                observeSession()
            ) { selectedPackages, preferredDuration, activeSession ->
                Triple(selectedPackages, preferredDuration, activeSession)
            }.collect { (selectedPackages, preferredDuration, activeSession) ->
                _uiState.update { current ->
                    current.copy(
                        selectedAppCount = selectedPackages.size,
                        selectedDurationMinutes = if (preferredDuration > 0) preferredDuration else 45,
                        activeSessionStatus = activeSession?.status,
                        selectedActivity = activeSession?.activityType ?: current.selectedActivity,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeSmartwatch() {
        viewModelScope.launch {
            smartwatchSyncManager.connectedWatch.collect { watch ->
                _uiState.update { it.copy(smartwatchDevice = watch) }
            }
        }
    }

    private fun reconcileSession() {
        viewModelScope.launch {
            observeSession.reconcileOnStartup(
                nowMs = System.currentTimeMillis(),
                endOnExpiry = { _ -> }
            )
        }
    }

    fun checkAccessibilityService() {
        val isEnabled = FocusAccessibilityService.isServiceEnabled(context)
        _uiState.update { it.copy(isAccessibilityServiceEnabled = isEnabled) }
    }

    /**
     * Selección de actividad: [ Estudiar ] [ Trabajar ] [ Leer ].
     */
    fun onActivitySelected(activity: FocusActivityType) {
        _uiState.update { it.copy(selectedActivity = activity) }
    }

    /**
     * Actualiza la duración en minutos (ej. 45 min).
     */
    fun onDurationSelected(minutes: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedDurationMinutes = minutes) }
            restrictionStatus.savePreferredDuration(minutes)
        }
    }

    /**
     * Prueba la vibración del smartwatch.
     */
    fun onTestSmartwatchVibration() {
        smartwatchSyncManager.testHapticVibration()
    }

    /**
     * Inicia la sesión sincronizada en el teléfono y en el smartwatch.
     */
    fun onStartSession() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val selectedPackages = saveSelectedApps.loadCurrent()

            val result = startSession(
                durationMinutes = currentState.selectedDurationMinutes,
                selectedPackageNames = selectedPackages,
                activityType = currentState.selectedActivity
            )

            result.fold(
                onSuccess = { session ->
                    // Sincroniza el inicio en el smartwatch
                    smartwatchSyncManager.notifySessionStarted(
                        activityType = session.activityType,
                        durationMinutes = session.durationMinutes,
                        totalMillis = session.durationMinutes * 60_000L
                    )
                    _navigateToSession.value = true
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }

    fun onNavigatedToSession() {
        _navigateToSession.value = false
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
