package com.focus.app.domain.usecase

import com.focus.app.domain.repository.AppRestrictionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para guardar y recuperar las aplicaciones seleccionadas para restricción.
 */
class SaveSelectedAppsUseCase @Inject constructor(
    private val repository: AppRestrictionRepository
) {
    /**
     * Flujo de los paquetes actualmente seleccionados para observación reactiva.
     */
    val selectedPackageNames: Flow<Set<String>> = repository.selectedPackageNames

    /**
     * Persiste el conjunto de paquetes seleccionados.
     *
     * @param packageNames Conjunto de nombres de paquetes a guardar.
     */
    suspend operator fun invoke(packageNames: Set<String>) {
        repository.saveSelectedPackages(packageNames)
    }

    /**
     * Carga el conjunto de paquetes seleccionados de forma puntual.
     *
     * @return El conjunto persistido, o un conjunto vacío.
     */
    suspend fun loadCurrent(): Set<String> {
        return repository.loadSelectedPackages()
    }
}
