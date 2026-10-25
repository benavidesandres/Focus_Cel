package com.focus.app.data.repository

import com.focus.app.data.local.preferences.FocusPreferencesDataSource
import com.focus.app.domain.repository.AppRestrictionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de restricciones de aplicaciones.
 * Persiste las selecciones de paquetes y las preferencias del usuario usando DataStore.
 */
@Singleton
class AppRestrictionRepositoryImpl @Inject constructor(
    private val dataSource: FocusPreferencesDataSource
) : AppRestrictionRepository {

    override val selectedPackageNames: Flow<Set<String>> = dataSource.selectedPackages

    override suspend fun saveSelectedPackages(packageNames: Set<String>) {
        dataSource.saveSelectedPackages(packageNames)
    }

    override suspend fun loadSelectedPackages(): Set<String> {
        return dataSource.loadSelectedPackages()
    }

    override val preferredDurationMinutes: Flow<Int> = dataSource.preferredDurationMinutes

    override suspend fun savePreferredDuration(minutes: Int) {
        dataSource.savePreferredDuration(minutes)
    }
}
