package com.focus.app.platform.smartwatch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estado del detector de inactividad física / sedentarismo.
 *
 * @param isAlertActive Si la alerta de falta de movimiento está encendida.
 * @param minutesWithoutMovement Minutos transcurridos sin movimiento detectado.
 * @param thresholdMinutes Umbral de minutos tras el cual se lanza la alerta (ej. 45 min).
 * @param totalAlertsTriggered Número total de recordatorios emitidos durante la sesión.
 */
data class SedentarismStatus(
    val isAlertActive: Boolean = false,
    val minutesWithoutMovement: Int = 0,
    val thresholdMinutes: Int = 45,
    val totalAlertsTriggered: Int = 0
)

/**
 * Monitor inteligente de inactividad física para el smartwatch y la aplicación.
 *
 * Analiza el tiempo continuo de inmovilidad mientras el usuario estudia, trabaja o lee.
 * Cuando el usuario sobrepasa el umbral (por ejemplo 45 minutos seguidos sentado):
 * 1. Activa la alerta háptica en la muñeca ("Mucho tiempo sin moverte").
 * 2. Muestra un recordatorio sutil para estirarse o caminar 1 minuto.
 */
@Singleton
class SedentarismDetector @Inject constructor(
    private val hapticHelper: SmartwatchHapticHelper
) {
    private val _status = MutableStateFlow(SedentarismStatus())
    val status: StateFlow<SedentarismStatus> = _status.asStateFlow()

    private var sessionStartTimestampMs: Long = 0L
    private var lastMovementTimestampMs: Long = 0L

    /**
     * Inicia el monitoreo de movimiento al comenzar una sesión.
     *
     * @param thresholdMinutes Umbral configurado en minutos.
     */
    fun startMonitoring(thresholdMinutes: Int = 45) {
        val now = System.currentTimeMillis()
        sessionStartTimestampMs = now
        lastMovementTimestampMs = now
        _status.value = SedentarismStatus(
            isAlertActive = false,
            minutesWithoutMovement = 0,
            thresholdMinutes = thresholdMinutes,
            totalAlertsTriggered = 0
        )
    }

    /**
     * Evalúa el tiempo transcurrido en cada ciclo del temporizador.
     *
     * @param nowMs Timestamp actual.
     * @return true si se acaba de disparar una nueva alerta de inactividad.
     */
    fun evaluateStillness(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (sessionStartTimestampMs == 0L) return false

        val elapsedMs = nowMs - lastMovementTimestampMs
        val elapsedMinutes = (elapsedMs / 60_000L).toInt()

        val current = _status.value
        val shouldAlert = elapsedMinutes >= current.thresholdMinutes && !current.isAlertActive

        if (shouldAlert) {
            _status.update {
                it.copy(
                    isAlertActive = true,
                    minutesWithoutMovement = elapsedMinutes,
                    totalAlertsTriggered = it.totalAlertsTriggered + 1
                )
            }
            // Dispara vibración en el teléfono y en el smartwatch
            hapticHelper.onInactivityAlert()
            return true
        } else {
            _status.update {
                it.copy(minutesWithoutMovement = elapsedMinutes)
            }
            return false
        }
    }

    /**
     * Registra que se detectó movimiento (paso, sensor acelerómetro del smartwatch o interacción táctil).
     */
    fun onMovementDetected() {
        lastMovementTimestampMs = System.currentTimeMillis()
        _status.update {
            it.copy(
                isAlertActive = false,
                minutesWithoutMovement = 0
            )
        }
    }

    /**
     * Descarta la alerta activa (el usuario confirma que se estiró o continuará).
     */
    fun dismissAlert() {
        lastMovementTimestampMs = System.currentTimeMillis()
        _status.update {
            it.copy(
                isAlertActive = false,
                minutesWithoutMovement = 0
            )
        }
    }

    /**
     * Detiene el monitoreo al finalizar la sesión.
     */
    fun stopMonitoring() {
        sessionStartTimestampMs = 0L
        lastMovementTimestampMs = 0L
        _status.value = SedentarismStatus()
    }
}
