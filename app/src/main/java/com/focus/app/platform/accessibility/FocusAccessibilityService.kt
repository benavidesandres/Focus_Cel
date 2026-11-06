package com.focus.app.platform.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.focus.app.domain.model.SessionPhase
import com.focus.app.domain.session.FocusSessionManager
import com.focus.app.platform.apps.ProtectedPackages
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
 * EXCLUSIONES: ver [ProtectedPackages].
 */
@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var sessionManager: FocusSessionManager

    @Inject
    lateinit var protectedPackages: ProtectedPackages

    // Ámbito de coroutine del servicio — cancelado al destruir el servicio
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Última app vista en primer plano, para bloquearla si la sesión empieza con ella abierta
    private var foregroundPackage: String? = null
    private var neverRestricted: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        neverRestricted = protectedPackages.current()
        observeSessionStart()
    }

    /**
     * Una sesión iniciada desde el reloj puede comenzar con una app bloqueada ya abierta,
     * caso en el que no llega ningún evento de ventana nuevo.
     */
    private fun observeSessionStart() {
        serviceScope.launch {
            sessionManager.phase
                .map { phase -> (phase as? SessionPhase.Focusing)?.session?.id }
                .distinctUntilChanged()
                .collect { sessionId ->
                    if (sessionId != null) {
                        neverRestricted = protectedPackages.current()
                        foregroundPackage?.let(::restrictIfNeeded)
                    }
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Solo procesa cambios de ventana que indican un nuevo primer plano
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // La barra de estado y el panel rápido se superponen sin cambiar la app en primer plano
        if (packageName == SYSTEM_UI_PACKAGE) return

        foregroundPackage = packageName
        restrictIfNeeded(packageName)
    }

    private fun restrictIfNeeded(packageName: String) {
        if (packageName !in neverRestricted && sessionManager.isRestricted(packageName)) {
            launchRestrictionScreen(packageName)
        }
    }

    /**
     * Lanza la pantalla de restricción de Focus cuando se detecta una app bloqueada.
     *
     * @param blockedPackage El paquete que intentó pasar al primer plano.
     */
    private fun launchRestrictionScreen(blockedPackage: String) {
        val intent = Intent(this, RestrictionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(RestrictionActivity.EXTRA_BLOCKED_PACKAGE, blockedPackage)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // El servicio fue interrumpido por el sistema — no se requiere acción específica
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"

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
                ComponentName.unflattenFromString(service) == expectedComponentName
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
