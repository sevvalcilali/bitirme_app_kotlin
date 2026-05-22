package com.example.bitirmeapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitirmeapp.ui.dashboard.components.*
import com.example.bitirmeapp.ui.theme.NabzBackground
import com.example.bitirmeapp.util.OturumYoneticisi

// Tek odaklı sade ekran (Nielsen 1994). Lavanta paleti sakinleştirici (Mehta & Zhu 2009).
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onStartEmaClick: () -> Unit,
    onDetailClick: (Long) -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var bilgiAcik by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!OturumYoneticisi.pasifBilgiGosterildiMi(context)) {
            bilgiAcik = true
        }
    }

    if (bilgiAcik) {
        PasifVeriBilgiDialog(onDismiss = {
            OturumYoneticisi.pasifBilgiyiIsaretle(context)
            bilgiAcik = false
        })
    }

    Scaffold(
        containerColor = NabzBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Üst başlık.
            item {
                UserHeader(
                    userName = uiState.userName,
                    date = uiState.todayDate,
                    onSettingsClick = onSettingsClick
                )
            }

            // Ana kart: EMA yapılmadıysa başlat kartı, yapıldıysa sonuç kartı.
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    when (val status = uiState.todayStatus) {
                        is TodayStatus.NotCompleted -> {
                            StartEmaCard(onStartClick = onStartEmaClick)
                        }
                        is TodayStatus.Completed -> {
                            ResultCard(
                                riskLevel = status.riskLevel,
                                riskLabel = status.riskLabel,
                                riskPercentage = status.riskPercentage,
                                timestamp = status.timestamp,
                                recommendation = status.recommendation,
                                onDetailClick = { onDetailClick(status.emaEntryId) }
                            )
                        }
                    }
                }
            }

            // Son 7 gün özeti.
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    WeeklyOverview(
                        last7Days = uiState.last7Days,
                        onHistoryClick = onHistoryClick
                    )
                }
            }
        }
    }
}
