package com.focus.app.domain.usecase

import com.focus.app.domain.repository.AppRestrictionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener las preferencias del usuario relacionadas con la duración.
 */
class GetRestrictionStatusUseCase @Inject constructor(
    private val restrictionRepository: AppRestrictionRepository
) {
    /**
     * Flujo observable de la duración de sesión preferida.
     */
    val preferredDurationMinutes: Flow<Int> = restrictionRepository.preferredDurationMinutes

    /**
     * Persiste la duración preferida del usuario.
     *
     * @param minutes Duración en minutos.
     */
    suspend fun savePreferredDuration(minutes: Int) {
        if (minutes > 0) {
            restrictionRepository.savePreferredDuration(minutes)
        }
    }
}
