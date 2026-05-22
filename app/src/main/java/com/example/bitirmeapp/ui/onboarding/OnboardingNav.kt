package com.example.bitirmeapp.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.bitirmeapp.ui.theme.NabzPrimary

// Onboarding alt-grafiği, 6 ekran sırayla:
// ad -> email -> sifre -> yas -> cinsiyet -> onay (GÖNDER).
// ViewModel parent entry'ye scope'lu, 6 ekran aynı instance'ı paylaşır.
// Son ekranda kayıt başarılı olunca onTamamlandi çağrılır.
const val ONBOARDING_ROUTE = "onboarding"
private const val OB_AD = "ob_ad"
private const val OB_EMAIL = "ob_email"
private const val OB_SIFRE = "ob_sifre"
private const val OB_YAS = "ob_yas"
private const val OB_CINSIYET = "ob_cinsiyet"
private const val OB_ONAY = "ob_onay"

fun NavGraphBuilder.onboardingGrafi(
    navController: NavHostController,
    onTamamlandi: () -> Unit
) {
    navigation(startDestination = OB_AD, route = ONBOARDING_ROUTE) {
        composable(OB_AD) {
            val vm = navController.onboardingViewModel()
            val s by vm.state.collectAsState()
            AdSoyadEkrani(
                adim = 1,
                deger = s.adSoyad,
                onDegisim = vm::setAdSoyad,
                onDevam = { navController.navigate(OB_EMAIL) }
            )
        }
        composable(OB_EMAIL) {
            val vm = navController.onboardingViewModel()
            val s by vm.state.collectAsState()
            EmailEkrani(
                adim = 2,
                deger = s.email,
                onDegisim = vm::setEmail,
                onDevam = { navController.navigate(OB_SIFRE) }
            )
        }
        composable(OB_SIFRE) {
            val vm = navController.onboardingViewModel()
            val s by vm.state.collectAsState()
            SifreEkrani(
                adim = 3,
                deger = s.sifre,
                onDegisim = vm::setSifre,
                onDevam = { navController.navigate(OB_YAS) }
            )
        }
        composable(OB_YAS) {
            val vm = navController.onboardingViewModel()
            val s by vm.state.collectAsState()
            YasEkrani(
                adim = 4,
                deger = s.yas,
                onDegisim = vm::setYas,
                onDevam = { navController.navigate(OB_CINSIYET) }
            )
        }
        composable(OB_CINSIYET) {
            val vm = navController.onboardingViewModel()
            val s by vm.state.collectAsState()
            CinsiyetEkrani(
                adim = 5,
                secili = s.cinsiyet,
                onDegisim = vm::setCinsiyet,
                onDevam = { navController.navigate(OB_ONAY) }
            )
        }
        composable(OB_ONAY) {
            val vm = navController.onboardingViewModel()
            val s by vm.state.collectAsState()
            LaunchedEffect(s.basariliMi) {
                if (s.basariliMi) {
                    vm.tuket()
                    onTamamlandi()
                }
            }
            OnayEkrani(
                adim = 6,
                onayli = s.onayli,
                onOnayDegisim = vm::setOnayli,
                hata = s.hata,
                isLoading = s.isLoading,
                onGonder = vm::kaydet
            )
        }
    }
}

@Composable
private fun NavHostController.onboardingViewModel(): OnboardingViewModel {
    // Key vermiyoruz: popUpTo(ONBOARDING) sonrası recompose'da getBackStackEntry
    // crash ediyordu. Key'siz remember ilk değeri tutar.
    val parentEntry = remember { getBackStackEntry(ONBOARDING_ROUTE) }
    return viewModel(parentEntry)
}

// ---------- Ortak iskelet ----------

@Composable
private fun OnboardingIskelet(
    adim: Int,
    baslik: String,
    aciklama: String? = null,
    hata: String? = null,
    butonMetni: String = "DEVAM",
    butonAktif: Boolean,
    isLoading: Boolean = false,
    onButon: () -> Unit,
    icerik: @Composable ColumnScope.() -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Kayıt Adımı $adim / 6",
                fontSize = 13.sp,
                color = NabzPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { adim / 6f },
                modifier = Modifier.fillMaxWidth(),
                color = NabzPrimary,
                trackColor = NabzPrimary.copy(alpha = 0.18f)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = baslik,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (aciklama != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = aciklama,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))
            icerik()
            if (hata != null) {
                Spacer(Modifier.height(12.dp))
                Text(text = hata, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onButon,
                enabled = butonAktif && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NabzPrimary,
                    disabledContainerColor = NabzPrimary.copy(alpha = 0.4f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = butonMetni,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------- 1: Ad soyad ----------
@Composable
private fun AdSoyadEkrani(
    adim: Int,
    deger: String,
    onDegisim: (String) -> Unit,
    onDevam: () -> Unit
) {
    OnboardingIskelet(
        adim = adim,
        baslik = "Adın ve soyadın nedir?",
        aciklama = "Hesabında görüntülenecek isim.",
        butonAktif = deger.trim().isNotBlank(),
        onButon = onDevam
    ) {
        OutlinedTextField(
            value = deger,
            onValueChange = onDegisim,
            label = { Text("Ad Soyad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
    }
}

// ---------- 2: E-posta ----------
@Composable
private fun EmailEkrani(
    adim: Int,
    deger: String,
    onDegisim: (String) -> Unit,
    onDevam: () -> Unit
) {
    val gecerli = deger.contains("@") && deger.substringAfterLast("@").contains(".")
    OnboardingIskelet(
        adim = adim,
        baslik = "E-posta adresin",
        aciklama = "Girişte bu e-posta ile oturum açacaksın.",
        butonAktif = gecerli,
        onButon = onDevam
    ) {
        OutlinedTextField(
            value = deger,
            onValueChange = onDegisim,
            label = { Text("E-posta") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
    }
}

// ---------- 3: Şifre ----------
@Composable
private fun SifreEkrani(
    adim: Int,
    deger: String,
    onDegisim: (String) -> Unit,
    onDevam: () -> Unit
) {
    OnboardingIskelet(
        adim = adim,
        baslik = "Şifre oluştur",
        aciklama = "En az 6 karakter. Sadece bu cihazda saklanır (hash'li).",
        butonAktif = deger.length >= 6,
        onButon = onDevam
    ) {
        OutlinedTextField(
            value = deger,
            onValueChange = onDegisim,
            label = { Text("Şifre") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
    }
}

// ---------- 4: Yaş ----------
@Composable
private fun YasEkrani(
    adim: Int,
    deger: String,
    onDegisim: (String) -> Unit,
    onDevam: () -> Unit
) {
    val sayi = deger.toIntOrNull()
    val kucuk = sayi != null && sayi < 18
    val gecerli = sayi != null && sayi in 18..99
    OnboardingIskelet(
        adim = adim,
        baslik = "Yaşın",
        aciklama = "Bu çalışma 18 yaş ve üzeri katılımcılar içindir.",
        hata = if (kucuk) "Bu çalışma 18 yaş ve üzeri içindir." else null,
        butonAktif = gecerli,
        onButon = onDevam
    ) {
        OutlinedTextField(
            value = deger,
            onValueChange = onDegisim,
            label = { Text("Yaş") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
    }
}

// ---------- 5: Cinsiyet ----------
@Composable
private fun CinsiyetEkrani(
    adim: Int,
    secili: String,
    onDegisim: (String) -> Unit,
    onDevam: () -> Unit
) {
    val secenekler = listOf("Kadın", "Erkek", "Belirtmek istemiyorum")
    OnboardingIskelet(
        adim = adim,
        baslik = "Cinsiyet",
        aciklama = "Bu bilgi opsiyoneldir, modele gönderilmez.",
        butonAktif = true,
        onButon = onDevam
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            secenekler.forEach { secenek ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (secili == secenek)
                                NabzPrimary.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .selectable(
                            selected = secili == secenek,
                            onClick = { onDegisim(secenek) }
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = secili == secenek,
                        onClick = { onDegisim(secenek) }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = secenek, fontSize = 15.sp)
                }
            }
        }
    }
}

// ---------- 6: Aydınlatma + Onay (GÖNDER) ----------
@Composable
private fun OnayEkrani(
    adim: Int,
    onayli: Boolean,
    onOnayDegisim: (Boolean) -> Unit,
    hata: String?,
    isLoading: Boolean,
    onGonder: () -> Unit
) {
    LocalSoftwareKeyboardController.current?.hide()
    OnboardingIskelet(
        adim = adim,
        baslik = "Aydınlatma ve Onay",
        butonMetni = "GÖNDER",
        butonAktif = onayli,
        isLoading = isLoading,
        hata = hata,
        onButon = onGonder
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋 Aydınlatma Metni",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bu uygulama bir bitirme tezi prototipidir; tıbbi tanı aracı " +
                        "DEĞİLDİR. Topladığımız EMA cevaplarınız ve telefon kullanım " +
                        "özetleriniz akademik amaçla anonim işlenir. Konuşma içeriği, " +
                        "mesaj metni veya GPS koordinatınız kaydedilmez. Verilerinizi " +
                        "istediğiniz an silebilirsiniz. Onay vermezseniz uygulamayı " +
                        "kullanamazsınız.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = onayli,
                            onClick = { onOnayDegisim(!onayli) }
                        )
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = onayli, onCheckedChange = onOnayDegisim)
                    Text(
                        text = "Okudum, onaylıyorum",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
