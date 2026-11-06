package com.focus.app.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.SmartwatchSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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
        private val KEY_SESSION_ACTIVITY = stringPreferencesKey("active_session_activity")

        // Preferencias de Smartwatch (Wear OS)
        private val KEY_WATCH_HAPTICS = booleanPreferencesKey("smartwatch_haptics_enabled")
        private val KEY_WATCH_INACTIVITY = booleanPreferencesKey("smartwatch_inactivity_enabled")
        private val KEY_WATCH_INACTIVITY_THRESHOLD = intPreferencesKey("smartwatch_inactivity_threshold_min")
        private val KEY_WATCH_REST_INDICATOR = booleanPreferencesKey("smartwatch_rest_indicator_enabled")

        // Valor por defecto de duración (45 minutos según especificación)
        const val DEFAULT_DURATION_MINUTES = 45
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
        status: String,
        activityType: String
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_ID] = id
            prefs[KEY_SESSION_START] = startMs
            prefs[KEY_SESSION_END] = endMs
            prefs[KEY_SESSION_DURATION] = durationMinutes
            prefs[KEY_SESSION_PACKAGES] = packageNames
            prefs[KEY_SESSION_STATUS] = status
            prefs[KEY_SESSION_ACTIVITY] = activityType
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

    /** Flujo de la sesión activa almacenada; emite null si no hay ninguna. */
    val activeSession: Flow<SessionSnapshot?> = dataStore.data
        .map(::toSessionSnapshot)
        .distinctUntilChanged()

    /**
     * Lee los metadatos de la sesión activa almacenada.
     *
     * @return La instantánea, o null si no hay sesión.
     */
    suspend fun loadActiveSession(): SessionSnapshot? = activeSession.first()

    private fun toSessionSnapshot(prefs: Preferences): SessionSnapshot? {
        return SessionSnapshot(
            id = prefs[KEY_SESSION_ID] ?: return null,
            startMs = prefs[KEY_SESSION_START] ?: return null,
            endMs = prefs[KEY_SESSION_END] ?: return null,
            durationMinutes = prefs[KEY_SESSION_DURATION] ?: return null,
            packageNames = prefs[KEY_SESSION_PACKAGES] ?: emptySet(),
            status = prefs[KEY_SESSION_STATUS] ?: return null,
            activityType = prefs[KEY_SESSION_ACTIVITY] ?: FocusActivityType.WORK.id
        )
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
            prefs.remove(KEY_SESSION_ACTIVITY)
        }
    }

    // ── Preferencias de Smartwatch ──────────────────────────────────────────

    /** Flujo de las preferencias del reloj. */
    val smartwatchSettings: Flow<SmartwatchSettings> = dataStore.data
        .map { prefs ->
            SmartwatchSettings(
                hapticsEnabled = prefs[KEY_WATCH_HAPTICS] ?: true,
                inactivityAlertsEnabled = prefs[KEY_WATCH_INACTIVITY] ?: true,
                inactivityThresholdMinutes = prefs[KEY_WATCH_INACTIVITY_THRESHOLD]
                    ?: SmartwatchSettings.DEFAULT_INACTIVITY_THRESHOLD_MINUTES,
                restIndicatorEnabled = prefs[KEY_WATCH_REST_INDICATOR] ?: true
            )
        }
        .distinctUntilChanged()

    suspend fun setSmartwatchHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_WATCH_HAPTICS] = enabled }
    }

    suspend fun setSmartwatchInactivityEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_WATCH_INACTIVITY] = enabled }
    }

    suspend fun setSmartwatchInactivityThreshold(minutes: Int) {
        dataStore.edit { it[KEY_WATCH_INACTIVITY_THRESHOLD] = minutes }
    }

    suspend fun setSmartwatchRestIndicatorEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_WATCH_REST_INDICATOR] = enabled }
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
        val status: String,
        val activityType: String
    )
}
