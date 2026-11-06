package com.focus.app.domain.usecase

import com.focus.app.domain.model.InstalledApp
import com.focus.app.domain.repository.InstalledAppsRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener la lista de aplicaciones instaladas lanzables.
 * Delega en el repositorio, que aplica las exclusiones y el orden.
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
}
