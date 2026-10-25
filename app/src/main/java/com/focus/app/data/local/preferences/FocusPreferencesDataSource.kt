package com.focus.app.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuente de datos local usando DataStore Preferences.
 * Centraliza todas las operaciones de lectura y escritura de preferencias persistidas.
 * No expone lógica de dominio; solo trabaja con tipos primitivos.
 */
@Singleton
class FocusPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // ── Claves de preferencias ────────────────────────────────────────────────

    companion object {
        // Paquetes seleccionados para restricción
        private val KEY_SELECTED_PACKAGES = stringSetPreferencesKey("selected_packages")

        // Duración preferida de sesión en minutos
        private val KEY_PREFERRED_DURATION = intPreferencesKey("preferred_duration_minutes")

        // Metadatos de sesión activa
        private val KEY_SESSION_ID = longPreferencesKey("active_session_id")
        private val KEY_SESSION_START = longPreferencesKey("active_session_start_ms")
        private val KEY_SESSION_END = longPreferencesKey("active_session_end_ms")
        private val KEY_SESSION_DURATION = intPreferencesKey("active_session_duration_min")
        private val KEY_SESSION_STATUS = stringPreferencesKey("active_session_status")
        private val KEY_SESSION_PACKAGES = stringSetPreferencesKey("active_session_packages")

        // Valor por defecto de duración (25 minutos — preset Pomodoro)
        const val DEFAULT_DURATION_MINUTES = 25
    }

    // ── Paquetes seleccionados ────────────────────────────────────────────────

    /** Flujo de los paquetes seleccionados persistidos. */
    val selectedPackages: Flow<Set<String>> = dataStore.data
        .map { prefs -> prefs[KEY_SELECTED_PACKAGES] ?: emptySet() }

    /** Persiste el conjunto de paquetes seleccionados. */
    suspend fun saveSelectedPackages(packages: Set<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_PACKAGES] = packages
        }
    }

    /** Carga el conjunto de paquetes seleccionados de forma puntual. */
    suspend fun loadSelectedPackages(): Set<String> {
        return dataStore.data.first()[KEY_SELECTED_PACKAGES] ?: emptySet()
    }

    // ── Duración preferida ────────────────────────────────────────────────────

    /** Flujo de la duración de sesión preferida. */
    val preferredDurationMinutes: Flow<Int> = dataStore.data
        .map { prefs -> prefs[KEY_PREFERRED_DURATION] ?: DEFAULT_DURATION_MINUTES }

    /** Persiste la duración preferida del usuario. */
    suspend fun savePreferredDuration(minutes: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_PREFERRED_DURATION] = minutes
        }
    }

    // ── Sesión activa ─────────────────────────────────────────────────────────

    /**
     * Persiste los metadatos de una sesión activa.
     * Todos los campos se guardan en una sola transacción atómica.
     */
    suspend fun saveActiveSession(
        id: Long,
        startMs: Long,
        endMs: Long,
        durationMinutes: Int,
        packageNames: Set<String>,
        status: String
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_ID] = id
            prefs[KEY_SESSION_START] = startMs
            prefs[KEY_SESSION_END] = endMs
            prefs[KEY_SESSION_DURATION] = durationMinutes
            prefs[KEY_SESSION_PACKAGES] = packageNames
            prefs[KEY_SESSION_STATUS] = status
        }
    }

    /**
     * Actualiza solo el estado de la sesión activa persistida.
     *
     * @param status El nuevo estado como cadena.
     */
    suspend fun updateSessionStatus(status: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_STATUS] = status
        }
    }

    /**
     * Lee los metadatos de la sesión activa almacenada.
     * Devuelve null si no existe ninguna sesión persistida.
     *
     * @return Mapa de preferencias o null si no hay sesión.
     */
    suspend fun loadActiveSession(): SessionSnapshot? {
        val prefs = dataStore.data.first()
        val id = prefs[KEY_SESSION_ID] ?: return null
        val startMs = prefs[KEY_SESSION_START] ?: return null
        val endMs = prefs[KEY_SESSION_END] ?: return null
        val duration = prefs[KEY_SESSION_DURATION] ?: return null
        val packages = prefs[KEY_SESSION_PACKAGES] ?: emptySet()
        val status = prefs[KEY_SESSION_STATUS] ?: return null

        return SessionSnapshot(id, startMs, endMs, duration, packages, status)
    }

    /** Elimina todos los metadatos de la sesión activa en una sola transacción. */
    suspend fun clearActiveSession() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_SESSION_ID)
            prefs.remove(KEY_SESSION_START)
            prefs.remove(KEY_SESSION_END)
            prefs.remove(KEY_SESSION_DURATION)
            prefs.remove(KEY_SESSION_PACKAGES)
            prefs.remove(KEY_SESSION_STATUS)
        }
    }

    /**
     * Instantánea inmutable de los datos de sesión leídos de DataStore.
     * Evita lecturas parciales al mapear al modelo de dominio.
     */
    data class SessionSnapshot(
        val id: Long,
        val startMs: Long,
        val endMs: Long,
        val durationMinutes: Int,
        val packageNames: Set<String>,
        val status: String
    )
}
