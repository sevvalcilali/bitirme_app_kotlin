package com.example.bitirmeapp.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

// Alt menü bar konfigürasyonu (ikon, etiket, route) burada toplu duruyor.
sealed class MenuBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : MenuBarItem(route = "home", label = "Ana Sayfa", icon = Icons.Default.Home)
    object Ema : MenuBarItem(route = "ema_intro", label = "EMA", icon = Icons.AutoMirrored.Filled.List)
    object Settings : MenuBarItem(route = "settings", label = "Ayarlar", icon = Icons.Default.Settings)
}

// Bardaki sekmeler, soldan sağa sırayla.
val menuBarItems: List<MenuBarItem> = listOf(
    MenuBarItem.Home,
    MenuBarItem.Ema,
    MenuBarItem.Settings
)

// Bu route'larda alt bar gizleniyor (odak gereken ekranlar).
val routesWithoutMenuBar: Set<String> = setOf(
    "ema_questions",
    "ema_result/{entryId}"
)

fun shouldShowMenuBar(route: String?): Boolean = route !in routesWithoutMenuBar

@Composable
fun MenuBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        menuBarItems.forEach { item ->
            val secili = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
                selected = secili,
                onClick = {
                    if (secili) return@NavigationBarItem
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                            saveState = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
