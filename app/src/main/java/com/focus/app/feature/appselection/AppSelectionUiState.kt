package com.focus.app.feature.appselection

import com.focus.app.domain.model.InstalledApp

/**
 * Estado inmutable de la UI de la pantalla de selección de aplicaciones.
 *
 * @param allApps Lista completa de aplicaciones instaladas lanzables.
 * @param filteredApps Lista filtrada por el texto de búsqueda actual.
 * @param selectedPackageNames Conjunto de paquetes seleccionados para restricción.
 * @param searchQuery Texto de búsqueda actual.
 * @param isLoading Si la pantalla está cargando la lista de apps.
 * @param errorMessage Mensaje de error, o null si no hay error.
 */
data class AppSelectionUiState(
    val allApps: List<InstalledApp> = emptyList(),
    val filteredApps: List<InstalledApp> = emptyList(),
    val selectedPackageNames: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    /** Número de aplicaciones seleccionadas actualmente. */
    val selectedCount: Int get() = selectedPackageNames.size

    /** true si la lista filtrada está vacía y no hay error ni carga en progreso. */
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && filteredApps.isEmpty()

    /** true si hay texto de búsqueda activo. */
    val isSearching: Boolean get() = searchQuery.isNotEmpty()
}
