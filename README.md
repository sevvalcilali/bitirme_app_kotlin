# Nabz — EMA Ruh Hali Takip Uygulaması

Android (Kotlin + Jetpack Compose) tabanlı, EMA (Ecological Momentary Assessment)
ile ruh hali / mental sağlık takip uygulaması. Kullanıcı günde 2 kez kısa bir anket
doldurur, telefon arka planda pasif davranış verisi toplar; ikisi birleşip Python ML
sunucusuna gönderilir ve risk tahmini + öneri döner.

> Ayrıntılı mimari ve akış için: [`PROJE_DOKUMANTASYON.md`](PROJE_DOKUMANTASYON.md)

---

## Gereksinimler

| Araç | Sürüm |
|------|-------|
| Android Studio | Ladybug (2024.2) veya üzeri önerilir |
| JDK | 11+ |
| Gradle | 9.1.0 (wrapper ile otomatik iner) |
| Android SDK | compileSdk 34 / minSdk 24 |

`local.properties` repoda **yoktur** — Android Studio projeyi açınca SDK yolunu
kendi makinene göre otomatik üretir. Elle bir şey yapman gerekmez.

---

## Çalıştırma (emülatör)

1. Projeyi klonla:
   ```bash
   git clone https://github.com/sevvalcilali/bitirme_app_kotlin.git
   ```
2. Android Studio ile aç, Gradle sync'in bitmesini bekle.
3. Bir emülatör başlat (API 24+ herhangi bir cihaz).
4. **BASE_URL'i ayarla** (aşağıdaki bölüm) — yoksa arayüz açılır ama backend
   özellikleri çalışmaz.
5. **Run ▶** ile çalıştır.

---

## ⚠️ BASE_URL ayarı (önemli)

Backend adresi şu dosyada sabit kodludur:

`app/src/main/java/com/example/bitirmeapp/data/remote/RetrofitClient.kt`

```kotlin
private const val BASE_URL = "http://10.233.113.22:8000/"
```

Bunu **kendi ortamına göre** değiştir:

| Ortam | BASE_URL |
|-------|----------|
| **Emülatör** (backend aynı bilgisayarda) | `http://10.0.2.2:8000/` |
| **Gerçek cihaz** (backend aynı Wi-Fi ağında) | `http://<bilgisayarın-LAN-IP'si>:8000/` örn. `http://192.168.1.20:8000/` |

> `10.0.2.2`, emülatörden host bilgisayarın `localhost`'una karşılık gelen özel
> adrestir. LAN IP'ni `ipconfig` (Windows) / `ifconfig` veya `ip addr` (macOS/Linux)
> ile öğrenebilirsin.

Uygulama `http` (cleartext) kullanır; bu manifest'te zaten açıktır
(`usesCleartextTraffic="true"`).

---

## Backend

Tam çalışma için Python ML sunucusunun **8000 portunda** ayakta olması gerekir
(`/predict/mobile` endpoint'i). Sunucu kapalıyken:

- Arayüz, kayıt ve giriş ekranları açılır.
- EMA gönderimi (`BİTİR`) başarısız olur ve yarım kayıt geri silinir (günlük dilim
  harcanmaz). Ayrıntı için dokümantasyondaki "Option-A hata davranışı" bölümüne bak.

> Not: Backend bu repoda değildir; ayrı çalıştırılır.

---

## Proje yapısı (özet)

```
app/src/main/java/com/example/bitirmeapp/
├── data/
│   ├── local/    → Room (kullanıcı, EMA kayıtları)
│   ├── pasif/    → arka plan pasif veri toplama servisi
│   └── remote/   → Retrofit / RetrofitClient (BASE_URL burada)
├── ui/
│   ├── auth/         → kayıt + giriş
│   ├── izinler/      → izin ekranı
│   ├── onboarding/
│   ├── dashboard/    → ana ekran
│   ├── ema/          → 7 soruluk EMA akışı
│   └── screens/      → raporlar vb.
└── util/
```
