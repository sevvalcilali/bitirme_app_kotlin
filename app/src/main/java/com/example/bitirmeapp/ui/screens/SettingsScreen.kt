package com.example.bitirmeapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onCikis: () -> Unit = {}) {
    var cikisDialogAcik by remember { mutableStateOf(false) }

    if (cikisDialogAcik) {
        AlertDialog(
            onDismissRequest = { cikisDialogAcik = false },
            title = { Text("Çıkış Yap") },
            text = { Text("Hesabından çıkış yapmak istediğine emin misin?") },
            confirmButton = {
                TextButton(onClick = {
                    cikisDialogAcik = false
                    onCikis()
                }) {
                    Text("Çıkış Yap", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cikisDialogAcik = false }) { Text("Vazgeç") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ayarlar", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                SettingsHeader("Hesap")
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Profil Bilgileri",
                    subtitle = "Ad, e-posta ve şifre işlemleri"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Bildirimler",
                    subtitle = "Hatırlatıcılar ve uyarılar"
                )
            }

            item {
                SettingsHeader("Uygulama")
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Görünüm",
                    subtitle = "Koyu tema ve renk seçenekleri"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Hakkında",
                    subtitle = "Uygulama versiyonu ve lisanslar"
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ListItem(
                    headlineContent = { 
                        Text(
                            "Çıkış Yap", 
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    leadingContent = { 
                        Icon(
                            Icons.Default.ExitToApp, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.error
                        ) 
                    },
                    modifier = Modifier.clickable { cikisDialogAcik = true }
                )
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier.clickable { onClick() }
    )
}
