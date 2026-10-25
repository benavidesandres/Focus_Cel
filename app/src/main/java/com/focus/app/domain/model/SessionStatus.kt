package com.focus.app.domain.model

/**
 * Enumera los estados posibles del ciclo de vida de una sesión de enfoque.
 * Se usa un enum en lugar de booleanos para evitar combinaciones inválidas.
 */
enum class SessionStatus {
    /**
     * No hay sesión activa. El usuario puede configurar una nueva.
     */
    IDLE,

    /**
     * La sesión está activa y el temporizador está corriendo.
     * Las restricciones están activas si el servicio de accesibilidad está habilitado.
     */
    ACTIVE,

    /**
     * La sesión finalizó correctamente porque el temporizador llegó a cero.
     * Las restricciones fueron eliminadas.
     */
    COMPLETED,

    /**
     * La sesión fue terminada manualmente por el usuario antes de que
     * el temporizador llegara a cero.
     * Las restricciones fueron eliminadas.
     */
    INTERRUPTED
}
