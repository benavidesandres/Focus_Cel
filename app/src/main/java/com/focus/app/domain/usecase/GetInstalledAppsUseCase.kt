package com.focus.app.domain.usecase

import com.focus.app.domain.model.InstalledApp
import com.focus.app.domain.repository.InstalledAppsRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener la lista de aplicaciones instaladas lanzables.
 * Delega en el repositorio y añade reglas de negocio mínimas (exclusiones, orden).
 */
class GetInstalledAppsUseCase @Inject constructor(
    private val repository: InstalledAppsRepository
) {
    /**
     * Ejecuta la obtención de aplicaciones instaladas.
     *
     * @return Resultado con la lista ordenada o un error descriptivo.
     */
    suspend operator fun invoke(): Result<List<InstalledApp>> {
        return repository.getInstalledApps()
    }

    /**
     * Ejecuta la búsqueda filtrada de aplicaciones instaladas.
     *
     * @param query Texto de búsqueda.
     * @return Resultado con la lista filtrada o un error descriptivo.
     */
    suspend fun search(query: String): Result<List<InstalledApp>> {
        if (query.isBlank()) return invoke()
        return repository.searchApps(query)
    }
}
