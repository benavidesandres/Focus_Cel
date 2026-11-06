package com.focus.app.feature.appselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus.app.domain.model.InstalledApp
import com.focus.app.domain.usecase.GetInstalledAppsUseCase
import com.focus.app.domain.usecase.SaveSelectedAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

            val persistedSelections = saveSelectedApps.loadCurrent()

            getInstalledApps().fold(
                onSuccess = { apps ->
                    // Descarta las apps desinstaladas desde la última selección
                    val installedPackages = apps.mapTo(mutableSetOf()) { it.packageName }
                    val validSelections = persistedSelections.intersect(installedPackages)
                    if (validSelections != persistedSelections) saveSelectedApps(validSelections)

                    _uiState.update {
                        it.copy(
                            allApps = apps,
                            filteredApps = filter(apps, it.searchQuery),
                            selectedPackageNames = validSelections,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
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
                    _uiState.update { it.copy(filteredApps = filter(it.allApps, query)) }
                }
        }
    }

    /**
     * Filtra una lista de apps por nombre o nombre de paquete.
     *
     * @param apps Lista a filtrar.
     * @param query Texto de búsqueda (insensible a mayúsculas).
     * @return Las apps que coinciden, o todas si la búsqueda está vacía.
     */
    private fun filter(apps: List<InstalledApp>, query: String): List<InstalledApp> {
        if (query.isBlank()) return apps
        return apps.filter { app ->
            app.displayName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
        }
    }

    /**
     * Actualiza el texto de búsqueda.
     *
     * @param query Nuevo texto de búsqueda ingresado por el usuario.
     */
    fun onSearchQueryChanged(query: String) {
        // El campo de texto se actualiza al instante; solo el filtrado usa debounce
        _uiState.update { it.copy(searchQuery = query) }
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
