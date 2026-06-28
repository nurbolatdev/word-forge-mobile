package com.wordforge.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wordforge.ui.navigation.NavRoutes

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem(NavRoutes.LISTS, "Lists", Icons.Default.MenuBook),
    NavItem(NavRoutes.QUIZ_SELECT, "Practice", Icons.Default.Bolt),
    NavItem(NavRoutes.PROFILE, "Profile", Icons.Default.Person)
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar {
        navItems.forEach { item ->
            val isSelected = when (item.route) {
                NavRoutes.QUIZ_SELECT -> currentRoute?.startsWith("main/quiz") == true
                else -> currentRoute == item.route
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(NavRoutes.LISTS) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
