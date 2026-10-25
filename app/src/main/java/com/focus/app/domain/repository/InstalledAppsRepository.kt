package com.focus.app.domain.repository

import com.focus.app.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de aplicaciones instaladas.
 * El dominio depende de esta abstracción; la implementación vive en la capa de plataforma.
 */
interface InstalledAppsRepository {

    /**
     * Devuelve la lista de aplicaciones lanzables instaladas en el dispositivo.
     * Excluye la propia aplicación Focus y componentes del sistema no lanzables.
     * La carga se realiza en un dispatcher de IO apropiado.
     *
     * @return Resultado con la lista ordenada alfabéticamente, o un error descriptivo.
     */
    suspend fun getInstalledApps(): Result<List<InstalledApp>>

    /**
     * Busca aplicaciones cuyo nombre o paquete contenga la consulta.
     *
     * @param query Texto de búsqueda (insensible a mayúsculas).
     * @return Resultado con la lista filtrada y ordenada.
     */
    suspend fun searchApps(query: String): Result<List<InstalledApp>>
}
