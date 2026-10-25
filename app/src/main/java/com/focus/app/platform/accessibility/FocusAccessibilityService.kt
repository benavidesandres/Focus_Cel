package com.focus.app.platform.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.focus.app.domain.model.SessionStatus
import com.focus.app.domain.repository.FocusSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Servicio de accesibilidad de Focus para detectar aplicaciones en primer plano.
 *
 * PROPÓSITO: Durante una sesión activa, este servicio detecta cuándo el usuario
 * abre una aplicación restringida y muestra la pantalla de bloqueo de Focus.
 *
 * LIMITACIONES DEL SISTEMA:
 * - El usuario debe habilitar manualmente este servicio en Ajustes > Accesibilidad.
 * - Android puede desactivar el servicio en cualquier momento por razones de seguridad.
 * - El comportamiento varía entre fabricantes (Samsung, Xiaomi, etc.).
 * - No se garantiza la restricción absoluta; es un mecanismo de mejor esfuerzo.
 * - El servicio NUNCA lee contenido de pantalla, contraseñas ni datos personales.
 *
 * EXCLUSIONES DE RESTRICCIÓN:
 * - La propia aplicación Focus (evita bucles infinitos).
 * - El launcher del dispositivo.
 * - Ajustes del sistema de Android.
 * - La pantalla de bloqueo de Focus.
 * - Llamadas de emergencia.
 */
@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var sessionRepository: FocusSessionRepository

    // Ámbito de coroutine del servicio — cancelado al destruir el servicio
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Nombre del paquete de la pantalla de bloqueo de Focus
    private val restrictionActivityPackage = "com.focus.app"

    // Paquetes que nunca deben ser restringidos
    private val neverRestrictPackages = setOf(
        "android",
        "com.android.settings",
        "com.android.systemui",
        "com.android.phone",
        "com.android.dialer",
        restrictionActivityPackage
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Servicio conectado y listo para detectar eventos de ventana
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Solo procesa cambios de ventana que indican un nuevo primer plano
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return

        // Ignora paquetes del sistema y la propia app Focus
        if (packageName in neverRestrictPackages) return

        // Verifica si hay una sesión activa y si el paquete está restringido
        serviceScope.launch {
            val session = sessionRepository.activeSession.firstOrNull() ?: return@launch

            // Solo actúa si la sesión está activa
            if (session.status != SessionStatus.ACTIVE) return@launch

            // Verifica si el tiempo de sesión ya expiró
            val nowMs = System.currentTimeMillis()
            if (session.isExpired(nowMs)) return@launch

            // Verifica si el paquete en primer plano está en la lista de restringidos
            if (packageName !in session.selectedPackageNames) return@launch

            // Lanza la actividad de bloqueo de Focus
            launchRestrictionScreen(packageName, session.id)
        }
    }

    /**
     * Lanza la pantalla de restricción de Focus cuando se detecta una app bloqueada.
     *
     * @param blockedPackage El paquete que intentó pasar al primer plano.
     * @param sessionId ID de la sesión activa para mostrar información contextual.
     */
    private fun launchRestrictionScreen(blockedPackage: String, sessionId: Long) {
        val intent = Intent(this, RestrictionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(RestrictionActivity.EXTRA_BLOCKED_PACKAGE, blockedPackage)
            putExtra(RestrictionActivity.EXTRA_SESSION_ID, sessionId)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // El servicio fue interrumpido por el sistema — no se requiere acción específica
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancela todas las coroutines al destruir el servicio
        serviceScope.cancel()
    }

    companion object {
        /**
         * Verifica si el servicio de accesibilidad de Focus está habilitado.
         * Comprueba el ajuste del sistema, no solo el estado del proceso.
         *
         * @param context Contexto de la aplicación.
         * @return true si el servicio está habilitado según los ajustes del sistema.
         */
        fun isServiceEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, FocusAccessibilityService::class.java)
            val enabledServicesString = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesString)

            return colonSplitter.any { service ->
                val componentName = ComponentName.unflattenFromString(service)
                componentName?.equals(expectedComponentName) == true
            }
        }

        /**
         * Devuelve el Intent para abrir la pantalla de Ajustes de Accesibilidad de Android.
         * El usuario debe habilitar manualmente el servicio; la app nunca lo activa por sí misma.
         */
        fun openAccessibilitySettingsIntent(): Intent {
            return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
