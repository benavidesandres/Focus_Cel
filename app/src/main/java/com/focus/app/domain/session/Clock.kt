package com.focus.app.domain.session

/**
 * Fuente de tiempo actual, inyectable para poder probar la lógica temporal.
 */
fun interface Clock {
    fun nowMs(): Long
}
