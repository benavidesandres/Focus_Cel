package com.focus.app.domain.repository

import com.focus.app.domain.model.InstalledApp

/**
 * Interfaz del repositorio de aplicaciones instaladas.
 * El dominio depende de esta abstracción; la implementación vive en la capa de plataforma.
 */
interface InstalledAppsRepository {

    /**
     * Devuelve la lista de aplicaciones lanzables instaladas en el dispositivo.
     * Excluye la propia aplicación Focus y los paquetes que nunca deben bloquearse.
     * La carga se realiza en un dispatcher de IO apropiado.
     *
     * @return Resultado con la lista ordenada alfabéticamente, o un error descriptivo.
     */
    suspend fun getInstalledApps(): Result<List<InstalledApp>>
}
