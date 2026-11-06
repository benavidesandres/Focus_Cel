package com.focus.app.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.focus.app.core.di.ApplicationScope
import com.focus.app.data.repository.AppRestrictionRepositoryImpl
import com.focus.app.data.repository.FocusSessionRepositoryImpl
import com.focus.app.data.repository.SmartwatchSettingsRepositoryImpl
import com.focus.app.domain.repository.AppRestrictionRepository
import com.focus.app.domain.repository.FocusSessionRepository
import com.focus.app.domain.repository.InstalledAppsRepository
import com.focus.app.domain.repository.SessionForegroundHost
import com.focus.app.domain.repository.SmartwatchGateway
import com.focus.app.domain.repository.SmartwatchSettingsRepository
import com.focus.app.domain.session.Clock
import com.focus.app.platform.apps.InstalledAppsRepositoryImpl
import com.focus.app.platform.session.FocusSessionForegroundHost
import com.focus.app.platform.smartwatch.SmartwatchSyncManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

// Extensión de propiedad para obtener el DataStore de preferencias de Focus
private val Context.focusDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "focus_preferences",
    // Si el archivo se corrompe, se reinicia en lugar de dejar la app inutilizable
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
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
 * Módulo Hilt para proveer las dependencias de ejecución de toda la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object RuntimeModule {

    /** Provee el ámbito de coroutines que vive tanto como el proceso. */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /** Provee el reloj del sistema. */
    @Provides
    fun provideClock(): Clock = Clock(System::currentTimeMillis)
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

    /**
     * Vincula SmartwatchSettingsRepositoryImpl a la interfaz SmartwatchSettingsRepository.
     */
    @Binds
    @Singleton
    abstract fun bindSmartwatchSettingsRepository(
        impl: SmartwatchSettingsRepositoryImpl
    ): SmartwatchSettingsRepository

    /**
     * Vincula SmartwatchSyncManager a la interfaz SmartwatchGateway.
     */
    @Binds
    @Singleton
    abstract fun bindSmartwatchGateway(
        impl: SmartwatchSyncManager
    ): SmartwatchGateway

    /**
     * Vincula FocusSessionForegroundHost a la interfaz SessionForegroundHost.
     */
    @Binds
    @Singleton
    abstract fun bindSessionForegroundHost(
        impl: FocusSessionForegroundHost
    ): SessionForegroundHost
}
