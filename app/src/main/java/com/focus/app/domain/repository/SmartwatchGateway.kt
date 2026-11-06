package com.focus.app.domain.repository

import com.focus.app.domain.model.FocusSession
import com.focus.app.domain.model.SmartwatchCommand
import com.focus.app.domain.model.SmartwatchDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Puerta de enlace del dominio hacia el reloj.
 * Las llamadas nunca bloquean ni fallan: si el reloj no está disponible, se ignoran.
 */
interface SmartwatchGateway {

    /** Estado real del reloj detectado. */
    val device: StateFlow<SmartwatchDevice>

    /** Órdenes recibidas desde el reloj. Admite un único colector. */
    val commands: Flow<SmartwatchCommand>

    /** Vuelve a consultar qué relojes están alcanzables. */
    fun refreshConnection()

    /** Reproduce una vibración de prueba en el teléfono y en el reloj. */
    fun testVibration()

    /** Publica que comenzó una sesión y dispara la vibración de inicio. */
    fun onSessionStarted(session: FocusSession)

    /** Vuelve a publicar una sesión restaurada, sin vibración. */
    fun onSessionRestored(session: FocusSession)

    /** Sincroniza el tiempo restante para corregir la deriva del contador del reloj. */
    fun onProgress(session: FocusSession, remainingMillis: Long)

    /** Avisa que se alcanzó la mitad de la sesión. */
    fun onHalfwayReached()

    /** Avisa que el usuario lleva demasiado tiempo sin moverse. */
    fun onInactivityDetected()

    /** Avisa que comenzó el descanso guiado. */
    fun onRestStarted(session: FocusSession, breakEndTimestampMs: Long)

    /** Publica que ya no hay ninguna sesión en curso. */
    fun onSessionStopped()
}
