package com.focus.app.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.focus.app.app.navigation.FocusNavHost
import com.focus.app.core.designsystem.FocusTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de Focus.
 * Configura la pantalla de splash, el tema oscuro, el layout edge-to-edge
 * y el grafo de navegación de Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala la pantalla de splash de Android 12+ antes del setContentView
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Habilita el layout edge-to-edge para un aspecto inmersivo
        enableEdgeToEdge()

        setContent {
            FocusTheme {
                val navController = rememberNavController()

                FocusNavHost(
                    navController = navController
                )
            }
        }
    }
}
