package com.example.bitirmeapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bitirmeapp.ui.dashboard.DashboardScreen
import com.example.bitirmeapp.ui.ema.EmaIntroScreen
import com.example.bitirmeapp.ui.ema.EmaQuestionsScreen
import com.example.bitirmeapp.ui.ema.EmaResultScreen

private object Routes {
    const val HOME = "home"
    const val EMA_INTRO = "ema_intro"
    const val EMA_QUESTIONS = "ema_questions"
    const val EMA_RESULT = "ema_result/{entryId}"
    const val RAPORLAR = "raporlar/{entryId}"
    const val HAFTALIK_RAPOR = "haftalik_rapor"
    const val SETTINGS = "settings"

    fun emaResult(id: Long) = "ema_result/$id"
    fun raporlar(id: Long) = "raporlar/$id"
}

@Composable
fun MainScreen(onCikis: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (shouldShowMenuBar(currentRoute)) {
                MenuBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                DashboardScreen(
                    onStartEmaClick = {
                        navController.navigate(Routes.EMA_INTRO)
                    },
                    onDetailClick = { entryId ->
                        navController.navigate(Routes.raporlar(entryId))
                    },
                    onHistoryClick = {
                        navController.navigate(Routes.HAFTALIK_RAPOR)
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.SETTINGS)
                    }
                )
            }
            composable(Routes.EMA_INTRO) {
                EmaIntroScreen(
                    onStartClick = {
                        navController.navigate(Routes.EMA_QUESTIONS)
                    }
                )
            }
            composable(Routes.EMA_QUESTIONS) {
                EmaQuestionsScreen(
                    onBackToIntro = { navController.popBackStack() },
                    onComplete = { entryId ->
                        navController.navigate(Routes.emaResult(entryId)) {
                            popUpTo(Routes.EMA_INTRO) { inclusive = false }
                        }
                    }
                )
            }
            composable(
                route = Routes.EMA_RESULT,
                arguments = listOf(navArgument("entryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
                EmaResultScreen(
                    entryId = entryId,
                    onDone = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Routes.RAPORLAR,
                arguments = listOf(navArgument("entryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
                Raporlar(
                    entryId = entryId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.HAFTALIK_RAPOR) {
                HaftalikRapor(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onCikis = onCikis)
            }
        }
    }
}
