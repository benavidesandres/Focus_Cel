package com.focus.app.platform.smartwatch

import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.SmartwatchCommand
import com.focus.app.domain.model.SmartwatchProtocol
import org.json.JSONException
import org.json.JSONObject

/**
 * Mensajes del reloj ya interpretados.
 */
sealed interface SmartwatchMessage {

    /** Orden dirigida al gestor de sesiones. */
    data class Command(val command: SmartwatchCommand) : SmartwatchMessage

    /** Informe de estado del reloj; los campos ausentes son null. */
    data class Status(val model: String?, val batteryPercent: Int?) : SmartwatchMessage

    /** Comprobación de disponibilidad que debe responderse. */
    data object Ping : SmartwatchMessage
}

/**
 * Interpreta los mensajes recibidos del reloj.
 * Una carga útil inválida nunca debe tumbar la app, así que se trata como campos ausentes.
 */
object SmartwatchMessageParser {

    private const val KEY_DURATION_MINUTES = "durationMinutes"
    private const val KEY_ACTIVITY_ID = "activityId"
    private const val KEY_BATTERY = "battery"
    private const val KEY_MODEL = "model"

    /**
     * Interpreta un mensaje.
     *
     * @param path Ruta del mensaje.
     * @param data Carga útil en JSON; puede estar vacía.
     * @return El mensaje interpretado, o null si la ruta no es relevante para el teléfono.
     */
    fun parse(path: String, data: ByteArray): SmartwatchMessage? {
        return when (path) {
            SmartwatchProtocol.PATH_START_SESSION -> parseStart(parseJson(data))
            SmartwatchProtocol.PATH_STOP_SESSION ->
                SmartwatchMessage.Command(SmartwatchCommand.StopSession)
            SmartwatchProtocol.PATH_ACCELEROMETER_EVENT ->
                SmartwatchMessage.Command(SmartwatchCommand.MovementDetected)
            SmartwatchProtocol.PATH_DISMISS_ALERT ->
                SmartwatchMessage.Command(SmartwatchCommand.DismissInactivityAlert)
            SmartwatchProtocol.PATH_BATTERY,
            SmartwatchProtocol.PATH_DEVICE_STATUS -> parseStatus(parseJson(data))
            SmartwatchProtocol.PATH_PING -> SmartwatchMessage.Ping
            else -> null
        }
    }

    private fun parseStart(json: JSONObject): SmartwatchMessage {
        val duration = json.optInt(KEY_DURATION_MINUTES, 0).takeIf { it > 0 }
        val activity = json.optString(KEY_ACTIVITY_ID)
            .takeIf { it.isNotEmpty() }
            ?.let(FocusActivityType::fromId)

        return SmartwatchMessage.Command(SmartwatchCommand.StartSession(duration, activity))
    }

    private fun parseStatus(json: JSONObject): SmartwatchMessage {
        return SmartwatchMessage.Status(
            model = json.optString(KEY_MODEL).takeIf { it.isNotBlank() },
            batteryPercent = json.optInt(KEY_BATTERY, -1).takeIf { it in 0..100 }
        )
    }

    private fun parseJson(data: ByteArray): JSONObject {
        return try {
            JSONObject(data.toString(Charsets.UTF_8))
        } catch (_: JSONException) {
            JSONObject()
        }
    }
}
