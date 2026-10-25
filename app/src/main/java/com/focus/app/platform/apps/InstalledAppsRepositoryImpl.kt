package com.focus.app.platform.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.focus.app.BuildConfig
import com.focus.app.domain.model.InstalledApp
import com.focus.app.domain.repository.InstalledAppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de aplicaciones instaladas.
 * Usa Android PackageManager para enumerar las apps lanzables instaladas.
 *
 * Nota de privacidad: Se usa QUERY_ALL_PACKAGES para enumerar aplicaciones.
 * Este permiso es necesario porque las apps de usuario pertenecen a paquetes
 * arbitrarios que no podemos prever. Toda la información permanece en el
 * dispositivo y nunca se transmite a servidores externos.
 *
 * Nota de visibilidad: A partir de Android 11 (API 30), se aplican restricciones
 * de visibilidad de paquetes. QUERY_ALL_PACKAGES las supera garantizando que
 * podemos enumerar correctamente todas las apps instaladas del usuario.
 */
@Singleton
class InstalledAppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : InstalledAppsRepository {

    // Paquetes del sistema que nunca deben aparecer en la lista
    private val systemExclusions = setOf(
        "android",
        "com.android.settings",
        "com.android.systemui",
        "com.android.launcher",
        "com.android.launcher3",
        "com.google.android.launcher",
        "com.sec.android.app.launcher",
        "com.miui.home"
    )

    override suspend fun getInstalledApps(): Result<List<InstalledApp>> {
        return withContext(Dispatchers.IO) {
            try {
                val apps = queryLaunchableApps(query = null)
                Result.success(apps)
            } catch (e: Exception) {
                Result.failure(
                    Exception("No se pudieron cargar las aplicaciones instaladas: ${e.message}", e)
                )
            }
        }
    }

    override suspend fun searchApps(query: String): Result<List<InstalledApp>> {
        return withContext(Dispatchers.IO) {
            try {
                val apps = queryLaunchableApps(query = query.trim().lowercase())
                Result.success(apps)
            } catch (e: Exception) {
                Result.failure(
                    Exception("Error al buscar aplicaciones: ${e.message}", e)
                )
            }
        }
    }

    /**
     * Consulta el PackageManager para obtener aplicaciones lanzables.
     * Aplica exclusiones, deduplicación y filtro de búsqueda opcional.
     *
     * @param query Texto de búsqueda en minúsculas, o null para obtener todas.
     * @return Lista ordenada alfabéticamente de InstalledApp.
     */
    private fun queryLaunchableApps(query: String?): List<InstalledApp> {
        val pm = context.packageManager

        // Obtiene todas las actividades que responden al intent de lanzamiento
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedActivities = pm.queryIntentActivities(
            launchIntent,
            PackageManager.MATCH_ALL
        )

        // Conjunto para detectar paquetes duplicados
        val seenPackages = mutableSetOf<String>()

        return resolvedActivities
            .asSequence()
            .mapNotNull { info ->
                val packageName = info.activityInfo.packageName

                // Excluye la propia aplicación Focus
                if (packageName == BuildConfig.APPLICATION_PACKAGE) return@mapNotNull null

                // Excluye componentes del sistema
                if (packageName in systemExclusions) return@mapNotNull null

                // Deduplicación por nombre de paquete
                if (!seenPackages.add(packageName)) return@mapNotNull null

                // Intenta cargar el nombre y el ícono; descarta si falla
                try {
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    val displayName = pm.getApplicationLabel(appInfo).toString()

                    // Aplicar filtro de búsqueda si existe
                    if (query != null && !displayName.lowercase().contains(query) &&
                        !packageName.lowercase().contains(query)
                    ) {
                        return@mapNotNull null
                    }

                    val icon = try {
                        pm.getApplicationIcon(packageName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }

                    InstalledApp(
                        packageName = packageName,
                        displayName = displayName,
                        icon = icon
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    // Paquete fue desinstalado entre la consulta y la lectura
                    null
                }
            }
            .sortedBy { it.displayName.lowercase() }
            .toList()
    }
}
