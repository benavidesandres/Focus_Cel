package com.focus.app.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.BuildConfig
import com.focus.app.domain.usecase.GetRestrictionStatusUseCase
import com.focus.app.domain.usecase.SaveSelectedAppsUseCase
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
 * ViewModel para la pantalla de configuración de Focus.
 *
 * Administra las preferencias globales, monitorea el estado del servicio de accesibilidad
 * y permite actualizar la duración predeterminada de las sesiones.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restrictionStatus: GetRestrictionStatusUseCase,
    private val saveSelectedApps: SaveSelectedAppsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appVersion = BuildConfig.VERSION_NAME
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        checkAccessibilityService()
    }

    /**
     * Observa las preferencias almacenadas en DataStore y actualiza el estado reactivamente.
     */
    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                restrictionStatus.preferredDurationMinutes,
                saveSelectedApps.selectedPackageNames
            ) { duration, packages ->
                Pair(duration, packages.size)
            }.collect { (duration, packageCount) ->
                _uiState.update {
                    it.copy(
                        preferredDurationMinutes = duration,
                        selectedAppsCount = packageCount
                    )
                }
            }
        }
    }

    /**
     * Verifica si el servicio de accesibilidad de Focus está habilitado en los ajustes del sistema.
     * Se llama en cada reanudación de la pantalla para reflejar cambios fuera de la app.
     */
    fun checkAccessibilityService() {
        val isEnabled = FocusAccessibilityService.isServiceEnabled(context)
        _uiState.update { it.copy(isAccessibilityServiceEnabled = isEnabled) }
    }

    /**
     * Guarda la duración por defecto seleccionada por el usuario.
     */
    fun onDurationSelected(minutes: Int) {
        viewModelScope.launch {
            restrictionStatus.savePreferredDuration(minutes)
        }
    }
}
