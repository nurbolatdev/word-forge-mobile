package com.wordforge.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.wordforge.ui.auth.AuthViewModel
import com.wordforge.ui.auth.PhoneScreen
import com.wordforge.ui.auth.VerifyScreen

object NavRoutes {
    const val AUTH_GRAPH = "auth"
    const val PHONE = "auth/phone"
    const val VERIFY = "auth/verify"

    const val MAIN_GRAPH = "main"
    const val LISTS = "main/lists"
    const val LIST_DETAIL = "main/list/{listId}"
    const val QUIZ_SELECT = "main/quiz/select"
    const val QUIZ_MODE = "main/quiz/mode"
    const val QUIZ_PLAY = "main/quiz/play"
    const val PROFILE = "main/profile"

    fun listDetail(listId: Long) = "main/list/$listId"
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {

        // Auth graph — both screens share the same AuthViewModel instance
        navigation(startDestination = NavRoutes.PHONE, route = NavRoutes.AUTH_GRAPH) {
            composable(NavRoutes.PHONE) { entry ->
                val graphEntry = remember(entry) {
                    navController.getBackStackEntry(NavRoutes.AUTH_GRAPH)
                }
                val viewModel: AuthViewModel = hiltViewModel(graphEntry)
                PhoneScreen(viewModel, navController)
            }
            composable(NavRoutes.VERIFY) { entry ->
                val graphEntry = remember(entry) {
                    navController.getBackStackEntry(NavRoutes.AUTH_GRAPH)
                }
                val viewModel: AuthViewModel = hiltViewModel(graphEntry)
                VerifyScreen(viewModel, navController)
            }
        }

        // Main graph — populated in subsequent stages
        navigation(startDestination = NavRoutes.LISTS, route = NavRoutes.MAIN_GRAPH) {
            composable(NavRoutes.LISTS) {
                // Replaced in Stage 2
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("My Lists — coming in Stage 2", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
