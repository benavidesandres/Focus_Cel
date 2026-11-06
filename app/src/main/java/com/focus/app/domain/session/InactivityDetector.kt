package com.focus.app.domain.session

import com.focus.app.domain.model.SmartwatchSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estado del detector de inactividad física.
 *
 * @param isAlertActive Si la alerta de falta de movimiento está encendida.
 * @param minutesWithoutMovement Minutos transcurridos sin movimiento detectado.
 * @param thresholdMinutes Umbral de minutos tras el cual se lanza la alerta.
 */
data class InactivityStatus(
    val isAlertActive: Boolean = false,
    val minutesWithoutMovement: Int = 0,
    val thresholdMinutes: Int = SmartwatchSettings.DEFAULT_INACTIVITY_THRESHOLD_MINUTES
)

/**
 * Mide el tiempo continuo sin movimiento durante una sesión.
 * Solo decide cuándo alertar; las vibraciones las gestiona quien lo consulta.
 */
@Singleton
class InactivityDetector @Inject constructor() {

    private val _status = MutableStateFlow(InactivityStatus())
    val status: StateFlow<InactivityStatus> = _status.asStateFlow()

    // Timestamp del último movimiento, o null si no se está monitoreando
    @Volatile
    private var lastMovementMs: Long? = null

    /**
     * Inicia el monitoreo al comenzar una sesión.
     *
     * @param thresholdMinutes Umbral configurado en minutos.
     * @param nowMs Timestamp actual.
     */
    fun startMonitoring(thresholdMinutes: Int, nowMs: Long) {
        lastMovementMs = nowMs
        _status.value = InactivityStatus(thresholdMinutes = thresholdMinutes)
    }

    /**
     * Evalúa el tiempo transcurrido desde el último movimiento.
     *
     * @param nowMs Timestamp actual.
     * @return true si se acaba de disparar una nueva alerta.
     */
    fun evaluate(nowMs: Long): Boolean {
        val lastMovement = lastMovementMs ?: return false
        val minutesStill = ((nowMs - lastMovement) / 60_000L).toInt()
        val current = _status.value
        val shouldAlert = !current.isAlertActive && minutesStill >= current.thresholdMinutes

        _status.update {
            it.copy(
                minutesWithoutMovement = minutesStill,
                isAlertActive = it.isAlertActive || shouldAlert
            )
        }
        return shouldAlert
    }

    /**
     * Reinicia la medición: el usuario se movió o confirmó que se estiró.
     *
     * @param nowMs Timestamp actual.
     */
    fun onMovementDetected(nowMs: Long) {
        if (lastMovementMs == null) return
        lastMovementMs = nowMs
        _status.update { it.copy(isAlertActive = false, minutesWithoutMovement = 0) }
    }

    /** Detiene el monitoreo al finalizar la sesión. */
    fun stopMonitoring() {
        lastMovementMs = null
        _status.value = InactivityStatus()
    }
}
