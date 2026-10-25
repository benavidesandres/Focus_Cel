package com.focus.app.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.focus.app.data.local.preferences.FocusPreferencesDataSource
import com.focus.app.data.repository.AppRestrictionRepositoryImpl
import com.focus.app.data.repository.FocusSessionRepositoryImpl
import com.focus.app.domain.repository.AppRestrictionRepository
import com.focus.app.domain.repository.FocusSessionRepository
import com.focus.app.domain.repository.InstalledAppsRepository
import com.focus.app.platform.apps.InstalledAppsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Extensión de propiedad para obtener el DataStore de preferencias de Focus
private val Context.focusDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "focus_preferences"
)

/**
 * Módulo Hilt para proveer el DataStore de preferencias.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provee la instancia única de DataStore para toda la aplicación.
     * El DataStore es singleton por diseño de la librería.
     */
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.focusDataStore
    }
}

/**
 * Módulo Hilt para vincular interfaces de repositorio con sus implementaciones.
 * Usa @Binds para mayor eficiencia en comparación con @Provides.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Vincula FocusSessionRepositoryImpl a la interfaz FocusSessionRepository.
     */
    @Binds
    @Singleton
    abstract fun bindFocusSessionRepository(
        impl: FocusSessionRepositoryImpl
    ): FocusSessionRepository

    /**
     * Vincula AppRestrictionRepositoryImpl a la interfaz AppRestrictionRepository.
     */
    @Binds
    @Singleton
    abstract fun bindAppRestrictionRepository(
        impl: AppRestrictionRepositoryImpl
    ): AppRestrictionRepository

    /**
     * Vincula InstalledAppsRepositoryImpl a la interfaz InstalledAppsRepository.
     */
    @Binds
    @Singleton
    abstract fun bindInstalledAppsRepository(
        impl: InstalledAppsRepositoryImpl
    ): InstalledAppsRepository
}
