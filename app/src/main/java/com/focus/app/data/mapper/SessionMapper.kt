package com.focus.app.data.mapper

import com.focus.app.data.local.preferences.FocusPreferencesDataSource
import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus

/**
 * Funciones de mapeo entre el modelo de dominio y las representaciones persistidas.
 * Mantiene la lógica de conversión fuera de la fuente de datos y del repositorio.
 */
object SessionMapper {

    /**
     * Convierte un SessionSnapshot de DataStore al modelo de dominio FocusSession.
     *
     * @param snapshot La instantánea leída de DataStore.
     * @return El modelo de dominio, o null si el estado no es reconocido.
     */
    fun toDomain(snapshot: FocusPreferencesDataSource.SessionSnapshot): FocusSession? {
        val status = mapStatus(snapshot.status) ?: return null
        val activity = FocusActivityType.fromId(snapshot.activityType)

        return FocusSession(
            id = snapshot.id,
            startTimestampMs = snapshot.startMs,
            endTimestampMs = snapshot.endMs,
            durationMinutes = snapshot.durationMinutes,
            selectedPackageNames = snapshot.packageNames,
            status = status,
            activityType = activity
        )
    }

    /**
     * Convierte el estado del dominio a la cadena que se persiste en DataStore.
     *
     * @param status El estado del dominio.
     * @return La cadena lista para guardar.
     */
    fun toStatusString(status: SessionStatus): String {
        return status.name
    }

    /**
     * Convierte la representación de cadena del estado al enum del dominio.
     *
     * @param statusStr Cadena almacenada en DataStore.
     * @return El enum SessionStatus, o null si el valor es desconocido.
     */
    private fun mapStatus(statusStr: String): SessionStatus? {
        // Un estado desconocido indica datos corruptos o de una versión anterior
        return SessionStatus.entries.firstOrNull { it.name == statusStr }
    }
}
