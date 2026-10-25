package com.focus.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de restricción de aplicaciones.
 * Gestiona qué aplicaciones están seleccionadas para restricción y
 * coordina el estado de las preferencias de sesión del usuario.
 */
interface AppRestrictionRepository {

    /**
     * Flujo observable del conjunto de nombres de paquetes seleccionados.
     * Emite el conjunto actualizado cada vez que el usuario modifica la selección.
     */
    val selectedPackageNames: Flow<Set<String>>

    /**
     * Guarda el conjunto de paquetes seleccionados en DataStore.
     *
     * @param packageNames Conjunto de nombres de paquetes a persistir.
     */
    suspend fun saveSelectedPackages(packageNames: Set<String>)

    /**
     * Carga el conjunto de paquetes seleccionados de forma puntual.
     * Útil para inicialización antes de que el flujo emita.
     *
     * @return El conjunto de paquetes guardado, o un conjunto vacío.
     */
    suspend fun loadSelectedPackages(): Set<String>

    /**
     * Flujo observable de la duración de sesión preferida en minutos.
     */
    val preferredDurationMinutes: Flow<Int>

    /**
     * Guarda la duración de sesión preferida del usuario.
     *
     * @param minutes Duración en minutos.
     */
    suspend fun savePreferredDuration(minutes: Int)
}
