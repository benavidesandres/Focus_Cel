package com.focus.app.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.domain.usecase.GetRestrictionStatusUseCase
import com.focus.app.domain.usecase.ObserveFocusSessionUseCase
import com.focus.app.domain.usecase.SaveSelectedAppsUseCase
import com.focus.app.domain.usecase.StartFocusSessionUseCase
import com.focus.app.platform.accessibility.FocusAccessibilityService
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
 * ViewModel de la pantalla principal de Focus.
 * Coordina la selección de apps, la duración y el estado del servicio de accesibilidad.
 * Todos los efectos secundarios ocurren aquí, nunca en los composables.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeSession: ObserveFocusSessionUseCase,
    private val startSession: StartFocusSessionUseCase,
    private val saveSelectedApps: SaveSelectedAppsUseCase,
    private val restrictionStatus: GetRestrictionStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Eventos de un solo uso para navegación (no se mezclan en el estado de UI)
    private val _navigateToSession = MutableStateFlow(false)
    val navigateToSession: StateFlow<Boolean> = _navigateToSession.asStateFlow()

    init {
        // Combina todos los flujos relevantes en el estado de UI
        observeState()
        // Reconcilia la sesión persistida al iniciar
        reconcileSession()
    }

    /**
     * Combina los flujos de apps seleccionadas, duración preferida y sesión activa
     * en un único estado de UI actualizado reactivamente.
     */
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
                        selectedDurationMinutes = preferredDuration,
                        activeSessionStatus = activeSession?.status,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Reconcilia la sesión persistida al iniciar el ViewModel.
     * Si la sesión expiró, la limpia. Si sigue activa, navega a la pantalla de sesión.
     */
    private fun reconcileSession() {
        viewModelScope.launch {
            observeSession.reconcileOnStartup(
                nowMs = System.currentTimeMillis(),
                endOnExpiry = { _ ->
                    // La sesión expiró; el clearActiveSession ya fue llamado en el use case
                }
            )
        }
    }

    /**
     * Verifica el estado del servicio de accesibilidad.
     * Debe llamarse en onResume para reflejar cambios hechos en Ajustes.
     */
    fun checkAccessibilityService() {
        val isEnabled = FocusAccessibilityService.isServiceEnabled(context)
        _uiState.update { it.copy(isAccessibilityServiceEnabled = isEnabled) }
    }

    /**
     * Actualiza la duración de sesión seleccionada y la persiste.
     *
     * @param minutes La nueva duración en minutos.
     */
    fun onDurationSelected(minutes: Int) {
        viewModelScope.launch {
            restrictionStatus.savePreferredDuration(minutes)
        }
    }

    /**
     * Inicia una nueva sesión de enfoque con la configuración actual.
     * Si la validación falla, muestra un error en lugar de iniciar.
     */
    fun onStartSession() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val selectedPackages = saveSelectedApps.loadCurrent()

            val result = startSession(
                durationMinutes = currentState.selectedDurationMinutes,
                selectedPackageNames = selectedPackages
            )

            result.fold(
                onSuccess = {
                    // Navega a la pantalla de sesión activa
                    _navigateToSession.value = true
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }

    /** Consume el evento de navegación a sesión después de procesarlo. */
    fun onNavigatedToSession() {
        _navigateToSession.value = false
    }

    /** Limpia el mensaje de error actual. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
