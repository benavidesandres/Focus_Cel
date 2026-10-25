package com.focus.app.feature.appselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.domain.usecase.GetInstalledAppsUseCase
import com.focus.app.domain.usecase.SaveSelectedAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla de selección de aplicaciones.
 *
 * Carga las aplicaciones instaladas una sola vez al iniciar y las mantiene en memoria.
 * La búsqueda filtra la lista en memoria en lugar de consultar PackageManager repetidamente.
 * Las selecciones se persisten automáticamente cuando cambian.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val saveSelectedApps: SaveSelectedAppsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    // Flujo del texto de búsqueda para aplicar debounce y evitar filtrados excesivos
    private val _searchQuery = MutableStateFlow("")

    init {
        // Carga las apps y las selecciones persistidas al inicializar
        loadAppsAndSelections()
        // Inicia la observación reactiva del texto de búsqueda
        observeSearchQuery()
    }

    /**
     * Carga las aplicaciones instaladas y restaura las selecciones persistidas.
     * La carga solo ocurre una vez gracias a la caché del ViewModel.
     */
    private fun loadAppsAndSelections() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Carga en paralelo: apps instaladas + selecciones guardadas
            val persistedSelections = saveSelectedApps.loadCurrent()

            val result = getInstalledApps()

            result.fold(
                onSuccess = { apps ->
                    _uiState.update {
                        it.copy(
                            allApps = apps,
                            filteredApps = apps,
                            selectedPackageNames = persistedSelections,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Could not load apps"
                        )
                    }
                }
            )
        }
    }

    /**
     * Observa el flujo de búsqueda con debounce para filtrar eficientemente.
     * Filtra la lista ya cargada en memoria; no consulta PackageManager de nuevo.
     */
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(150L) // Debounce para evitar filtrar en cada tecla
                .distinctUntilChanged()
                .collect { query ->
                    filterApps(query)
                }
        }
    }

    /**
     * Filtra la lista de apps en memoria por nombre o nombre de paquete.
     *
     * @param query Texto de búsqueda (insensible a mayúsculas).
     */
    private fun filterApps(query: String) {
        val allApps = _uiState.value.allApps
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            val lowerQuery = query.lowercase()
            allApps.filter { app ->
                app.displayName.lowercase().contains(lowerQuery) ||
                    app.packageName.lowercase().contains(lowerQuery)
            }
        }

        _uiState.update { it.copy(filteredApps = filtered, searchQuery = query) }
    }

    /**
     * Actualiza el texto de búsqueda.
     *
     * @param query Nuevo texto de búsqueda ingresado por el usuario.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Alterna la selección de una aplicación.
     * Si está seleccionada, la deselecciona; si no, la selecciona.
     * Persiste el cambio automáticamente.
     *
     * @param packageName El nombre del paquete a alternar.
     */
    fun onToggleAppSelection(packageName: String) {
        viewModelScope.launch {
            val current = _uiState.value.selectedPackageNames
            val updated = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }

            _uiState.update { it.copy(selectedPackageNames = updated) }
            // Persiste la selección actualizada inmediatamente
            saveSelectedApps(updated)
        }
    }

    /**
     * Deselecciona todas las aplicaciones.
     */
    fun onClearAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedPackageNames = emptySet()) }
            saveSelectedApps(emptySet())
        }
    }

    /**
     * Reintenta cargar la lista de aplicaciones tras un error.
     */
    fun onRetry() {
        loadAppsAndSelections()
    }
}
