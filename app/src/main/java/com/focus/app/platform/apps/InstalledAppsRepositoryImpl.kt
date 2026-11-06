package com.focus.app.platform.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.focus.app.domain.model.InstalledApp
import com.focus.app.domain.repository.InstalledAppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de aplicaciones instaladas.
 * Usa Android PackageManager para enumerar las apps lanzables instaladas.
 *
 * Nota de visibilidad: a partir de Android 11 aplican restricciones de visibilidad de
 * paquetes. La etiqueta <queries> del manifiesto da acceso a las apps lanzables sin
 * necesitar el permiso sensible QUERY_ALL_PACKAGES. Los datos nunca salen del dispositivo.
 */
@Singleton
class InstalledAppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val protectedPackages: ProtectedPackages
) : InstalledAppsRepository {

    override suspend fun getInstalledApps(): Result<List<InstalledApp>> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(queryLaunchableApps())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Could not load installed apps: ${e.message}", e))
            }
        }
    }

    /**
     * Consulta el PackageManager para obtener aplicaciones lanzables.
     *
     * @return Lista sin duplicados ni paquetes protegidos, ordenada alfabéticamente.
     */
    private fun queryLaunchableApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val excludedPackages = protectedPackages.current()

        return packageManager.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
            .asSequence()
            .map { it.activityInfo.packageName }
            .distinct()
            .filterNot { it in excludedPackages }
            .mapNotNull { packageName -> loadApp(packageManager, packageName) }
            .sortedBy { it.displayName.lowercase() }
            .toList()
    }

    private fun loadApp(packageManager: PackageManager, packageName: String): InstalledApp? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            InstalledApp(
                packageName = packageName,
                displayName = packageManager.getApplicationLabel(appInfo).toString(),
                icon = packageManager.getApplicationIcon(appInfo)
            )
        } catch (e: PackageManager.NameNotFoundException) {
            // El paquete se desinstaló entre la consulta y la lectura
            null
        }
    }
}
