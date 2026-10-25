package com.focus.app.domain.usecase

import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.repository.FocusSessionRepository
import javax.inject.Inject

/**
 * Caso de uso para iniciar una nueva sesión de enfoque.
 * Valida las reglas de negocio antes de persistir la sesión.
 */
class StartFocusSessionUseCase @Inject constructor(
    private val sessionRepository: FocusSessionRepository
) {
    /**
     * Inicia una nueva sesión de enfoque si los parámetros son válidos.
     *
     * @param durationMinutes Duración de la sesión en minutos (debe ser positiva).
     * @param selectedPackageNames Paquetes seleccionados para restricción.
     * @param nowMs Timestamp actual en milisegundos (inyectado para testabilidad).
     * @return La sesión creada como éxito, o un error descriptivo.
     */
    suspend operator fun invoke(
        durationMinutes: Int,
        selectedPackageNames: Set<String>,
        activityType: com.focus.app.domain.model.FocusActivityType = com.focus.app.domain.model.FocusActivityType.TRABAJAR,
        nowMs: Long = System.currentTimeMillis()
    ): Result<FocusSession> {
        // Validación: la duración debe ser positiva
        if (durationMinutes <= 0) {
            return Result.failure(
                IllegalArgumentException("La duración de la sesión debe ser mayor que cero.")
            )
        }

        // Validación: la duración no puede superar 8 horas (límite razonable)
        if (durationMinutes > 480) {
            return Result.failure(
                IllegalArgumentException("La duración de la sesión no puede superar 8 horas.")
            )
        }

        // Nota: se permite iniciar sin apps seleccionadas.
        // En ese caso el temporizador corre pero no hay restricciones activas.
        // El ViewModel y la UI informan esto claramente al usuario.

        val session = FocusSession(
            id = nowMs,
            startTimestampMs = nowMs,
            endTimestampMs = nowMs + (durationMinutes * 60_000L),
            durationMinutes = durationMinutes,
            selectedPackageNames = selectedPackageNames,
            status = SessionStatus.ACTIVE,
            activityType = activityType,
            breakDurationMinutes = activityType.recommendedRestMinutes
        )

        sessionRepository.saveSession(session)

        return Result.success(session)
    }
}
