package com.example.bitirmeapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.data.pasif.PasifServisi
import com.example.bitirmeapp.data.pasif.PasifToplayici
import com.example.bitirmeapp.util.OturumYoneticisi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bitirmeapp.ui.auth.KimlikViewModel
import com.example.bitirmeapp.ui.izinler.IzinlerEkrani
import com.example.bitirmeapp.ui.onboarding.ONBOARDING_ROUTE
import com.example.bitirmeapp.ui.onboarding.onboardingGrafi
import com.example.bitirmeapp.ui.screens.LoginScreen
import com.example.bitirmeapp.ui.screens.MainScreen
import com.example.bitirmeapp.ui.theme.NabzTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Oturum varsa direkt main, yoksa izinler ekranından başla.
        val basla = if (OturumYoneticisi.girisYapildi(this)) "main" else "izinler"

        setContent {
            NabzTheme {
                val navController = rememberNavController()
                val kimlikVm: KimlikViewModel = viewModel()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = basla,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("izinler") {
                            val ctx = LocalContext.current
                            IzinlerEkrani(
                                onDevam = {
                                    PasifServisi.baslat(ctx)
                                    val prefs = ctx.getSharedPreferences(
                                        "pasif_prefs", Context.MODE_PRIVATE
                                    )
                                    if (!prefs.getBoolean("backfill_yapildi", false)) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            PasifToplayici.gecmisUsageBackfill(
                                                ctx.applicationContext
                                            )
                                            prefs.edit()
                                                .putBoolean("backfill_yapildi", true)
                                                .apply()
                                        }
                                    }
                                    // Önce login. Kayıt Ol'a basan onboarding'e gider.
                                    navController.navigate("login") {
                                        popUpTo("izinler") { inclusive = true }
                                    }
                                }
                            )
                        }
                        onboardingGrafi(
                            navController = navController,
                            onTamamlandi = {
                                navController.navigate("main") {
                                    popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                                }
                            }
                        )
                        composable("login") {
                            val state by kimlikVm.state.collectAsState()
                            LaunchedEffect(state.girisBasarili) {
                                if (state.girisBasarili) {
                                    kimlikVm.tuket()
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                            LoginScreen(
                                hata = state.hata,
                                isLoading = state.isLoading,
                                onLoginClick = { email, password ->
                                    kimlikVm.girisYap(email, password)
                                },
                                onRegisterClick = {
                                    kimlikVm.tuket()
                                    navController.navigate(ONBOARDING_ROUTE) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("main") {
                            val ctx = LocalContext.current
                            MainScreen(
                                onCikis = {
                                    OturumYoneticisi.cikis(ctx)
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
