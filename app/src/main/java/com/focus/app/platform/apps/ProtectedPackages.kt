package com.focus.app.platform.apps

import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Determina los paquetes que Focus nunca debe bloquear ni ofrecer para bloqueo.
 *
 * Garantiza que el usuario siempre pueda salir al inicio, hacer llamadas de emergencia
 * y acceder a los ajustes del sistema, sin importar el fabricante del dispositivo.
 */
@Singleton
class ProtectedPackages @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Calcula el conjunto actual; incluye el launcher y el marcador instalados en el dispositivo.
     *
     * @return Conjunto de paquetes protegidos.
     */
    fun current(): Set<String> = buildSet {
        addAll(SYSTEM_PACKAGES)
        add(context.packageName)
        addAll(launcherPackages())
        context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage?.let(::add)
    }

    private fun launcherPackages(): List<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager
            .queryIntentActivities(homeIntent, 0)
            .map { it.activityInfo.packageName }
    }

    private companion object {
        val SYSTEM_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.phone",
            "com.android.dialer",
            "com.android.emergency"
        )
    }
}
