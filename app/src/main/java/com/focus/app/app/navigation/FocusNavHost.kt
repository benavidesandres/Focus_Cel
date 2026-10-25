package com.focus.app.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.focus.app.feature.appselection.AppSelectionScreen
import com.focus.app.feature.home.HomeScreen
import com.focus.app.feature.session.FocusSessionScreen
import com.focus.app.feature.settings.SettingsScreen

/**
 * Grafo de navegación principal de Focus.
 * Define las transiciones entre pantallas con animaciones de deslizamiento y fundido.
 *
 * @param navController Controlador de navegación de Compose.
 */
@Composable
fun FocusNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = FocusDestination.Home.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeOut(tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(tween(200))
        }
    ) {
        // ── Pantalla principal ───────────────────────────────────────────────
        composable(FocusDestination.Home.route) {
            HomeScreen(
                onNavigateToAppSelection = {
                    navController.navigate(FocusDestination.AppSelection.route)
                },
                onNavigateToSession = {
                    navController.navigate(FocusDestination.ActiveSession.route)
                },
                onNavigateToSettings = {
                    navController.navigate(FocusDestination.Settings.route)
                }
            )
        }

        // ── Selección de aplicaciones ────────────────────────────────────────
        composable(FocusDestination.AppSelection.route) {
            AppSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Sesión activa ────────────────────────────────────────────────────
        composable(FocusDestination.ActiveSession.route) {
            FocusSessionScreen(
                onSessionEnded = {
                    // Regresa a la pantalla principal al terminar la sesión
                    navController.popBackStack(
                        route = FocusDestination.Home.route,
                        inclusive = false
                    )
                }
            )
        }

        // ── Configuración ────────────────────────────────────────────────────
        composable(FocusDestination.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
