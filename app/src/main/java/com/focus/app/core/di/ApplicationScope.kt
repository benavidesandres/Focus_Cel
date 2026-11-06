package com.focus.app.core.di

import javax.inject.Qualifier

/**
 * Identifica el CoroutineScope que vive tanto como el proceso de la aplicación.
 * Se usa para trabajo que no debe cancelarse al destruirse una pantalla.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
