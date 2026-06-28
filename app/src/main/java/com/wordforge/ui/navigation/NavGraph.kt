package com.wordforge.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.wordforge.ui.auth.AuthViewModel
import com.wordforge.ui.auth.PhoneScreen
import com.wordforge.ui.auth.VerifyScreen
import com.wordforge.ui.components.BottomNavBar
import com.wordforge.ui.lists.ListDetailScreen
import com.wordforge.ui.lists.ListsScreen
import com.wordforge.ui.profile.ProfileScreen
import com.wordforge.ui.quiz.ListSelectScreen
import com.wordforge.ui.quiz.ModeSelectScreen
import com.wordforge.ui.quiz.QuizScreen
import com.wordforge.ui.quiz.QuizViewModel

object NavRoutes {
    const val AUTH_GRAPH = "auth"
    const val PHONE = "auth/phone"
    const val VERIFY = "auth/verify"

    const val MAIN_GRAPH = "main"
    const val LISTS = "main/lists"
    const val LIST_DETAIL = "main/list/{listId}/{sourceLang}/{targetLang}/{listTitle}"
    const val QUIZ_GRAPH = "main/quiz"
    const val QUIZ_SELECT = "main/quiz/select"
    const val QUIZ_MODE = "main/quiz/mode"
    const val QUIZ_PLAY = "main/quiz/play"
    const val PROFILE = "main/profile"

    fun listDetail(listId: Long, sourceLang: String, targetLang: String, title: String) =
        "main/list/$listId/$sourceLang/$targetLang/${title.encodeForNav()}"
}

private fun String.encodeForNav() = java.net.URLEncoder.encode(this, "UTF-8")

private val bottomBarRoutes = setOf(
    NavRoutes.LISTS,
    NavRoutes.QUIZ_SELECT,
    NavRoutes.PROFILE
)

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) BottomNavBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth graph — Phone and Verify share one AuthViewModel
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

            // Main graph
            navigation(startDestination = NavRoutes.LISTS, route = NavRoutes.MAIN_GRAPH) {
                composable(NavRoutes.LISTS) {
                    ListsScreen(navController)
                }
                composable(
                    route = NavRoutes.LIST_DETAIL,
                    arguments = listOf(
                        navArgument("listId") { type = NavType.LongType },
                        navArgument("sourceLang") { type = NavType.StringType },
                        navArgument("targetLang") { type = NavType.StringType },
                        navArgument("listTitle") { type = NavType.StringType }
                    )
                ) { backStack ->
                    val args = backStack.arguments ?: return@composable
                    val listId = args.getLong("listId")
                    val sourceLang = args.getString("sourceLang") ?: "EN"
                    val targetLang = args.getString("targetLang") ?: "RU"
                    val listTitle = java.net.URLDecoder.decode(
                        args.getString("listTitle") ?: "", "UTF-8"
                    )
                    ListDetailScreen(
                        navController = navController,
                        listId = listId,
                        listTitle = listTitle,
                        sourceLang = sourceLang,
                        targetLang = targetLang
                    )
                }
                // Quiz sub-graph — all 3 screens share one QuizViewModel
                navigation(
                    startDestination = NavRoutes.QUIZ_SELECT,
                    route = NavRoutes.QUIZ_GRAPH
                ) {
                    composable(NavRoutes.QUIZ_SELECT) { entry ->
                        val graphEntry = remember(entry) {
                            navController.getBackStackEntry(NavRoutes.QUIZ_GRAPH)
                        }
                        val viewModel: QuizViewModel = hiltViewModel(graphEntry)
                        ListSelectScreen(viewModel, navController)
                    }
                    composable(NavRoutes.QUIZ_MODE) { entry ->
                        val graphEntry = remember(entry) {
                            navController.getBackStackEntry(NavRoutes.QUIZ_GRAPH)
                        }
                        val viewModel: QuizViewModel = hiltViewModel(graphEntry)
                        ModeSelectScreen(viewModel, navController)
                    }
                    composable(NavRoutes.QUIZ_PLAY) { entry ->
                        val graphEntry = remember(entry) {
                            navController.getBackStackEntry(NavRoutes.QUIZ_GRAPH)
                        }
                        val viewModel: QuizViewModel = hiltViewModel(graphEntry)
                        QuizScreen(viewModel, navController)
                    }
                }
                composable(NavRoutes.PROFILE) {
                    ProfileScreen(navController)
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
