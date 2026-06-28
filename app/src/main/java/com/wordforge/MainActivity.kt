package com.wordforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.wordforge.ui.navigation.NavGraph
import com.wordforge.ui.navigation.NavRoutes
import com.wordforge.ui.theme.WordForgeTheme
import com.wordforge.util.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = if (tokenManager.hasToken()) {
            NavRoutes.MAIN_GRAPH
        } else {
            NavRoutes.AUTH_GRAPH
        }

        setContent {
            WordForgeTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }
}
