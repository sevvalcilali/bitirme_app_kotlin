# Nabz — Proje Dokümantasyonu

EMA tabanlı ruh hali / mental sağlık takip uygulaması. Kullanıcı günde 2 kez kısa
bir anket (EMA) doldurur; telefon arka planda pasif davranış verisi toplar; ikisi
birleşip Python ML modeline gönderilir ve risk tahmini + öneri ekrana yazılır.

> Demo gerçek telefonda yapılır. Python sunucusu aynı Wi-Fi ağında açık olmalıdır.

---

## 1. Genel Akış (en baştan en sona)

```
1. İzinler ekranı        → kullanıcı izinleri verir, DEVAM ET
   └─ PasifServisi başlar (arka plan toplama)
   └─ Geçmiş UsageStats backfill (TEK SEFER, son 7 gün)
2. Kayıt Ol              → Room'a kullanıcı yazılır (şifre SHA-256 hash'li)
3. Giriş Yap             → e-posta + şifre Room'dan doğrulanır
4. Ana ekran (Dashboard) → "BAŞLA" → EMA Intro
5. EMA Intro             → günde 2 dilim (sabah <12:00, akşam ≥12:00) kapısı
6. EMA 7 soru            → stres, PAM, sosyal, PHQ-4 (4 soru)
7. BİTİR                 → cevaplar Room'a yazılır
                         → pasif veri epoch formatına çevrilir
                         → /predict/mobile'a POST (EMA + pasif + 7 günlük seri)
8. Cevap                 → risk tahmini Room'a güncellenir, sonuç ekranı açılır
9. Raporlar              → geçmiş kayıtlar + 3 katmanlı analiz gösterilir
```

**Hata davranışı (Option-A):** `/predict/mobile` başarısız olursa (sunucu yok,
internet yok) yarım EMA kaydı Room'dan **silinir** (günlük dilim harcanmaz),
kullanıcıya snackbar hatası gösterilir, sonuç ekranına geçilmez — tekrar denenebilir.

---

## 2. İzinler

### Manifest'te tanımlı (`AndroidManifest.xml`)

| İzin | Amaç |
|---|---|
| `INTERNET` | Python API'ye istek |
| `ACTIVITY_RECOGNITION` | Yürüme/durağan/koşma aktivite tanıma |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Hareketlilik / mesafe |
| `FOREGROUND_SERVICE` (+ `_LOCATION`, `_DATA_SYNC`) | Arka plan toplama servisi |
| `POST_NOTIFICATIONS` | Foreground service bildirimi |
| `RECEIVE_BOOT_COMPLETED` | (tanımlı) |
| `PACKAGE_USAGE_STATS` | Ekran/uygulama kullanım istatistiği (özel izin) |

### Kullanıcıdan istenen 3 izin (`IzinlerEkrani.kt`)

1. **Aktivite Tanıma** — runtime izin (`ACTIVITY_RECOGNITION` + konum), Android 10+.
2. **Konum** — runtime izin (`ACCESS_FINE/COARSE_LOCATION`).
3. **Kullanım İstatistiği** — özel izin; runtime ile verilemez, kullanıcı
   **Ayarlar → Kullanım erişimi** ekranına yönlendirilir (`ACTION_USAGE_ACCESS_SETTINGS`).

İzin verilmese de uygulama çalışır — backend eksik pasif feature'ları **popülasyon
medyanıyla** doldurur. İzin durumları ekran her açıldığında yeniden okunur.

---

## 3. Room (yerel veritabanı)

Veritabanı: `nabz.db`, **sürüm = 8**, `fallbackToDestructiveMigration` açık ama
asıl yol gerçek migration'lar.

### Tablolar

**`ema_entries`** (`EmaEntryEntity`) — bir EMA dolumu + dönen ML tahmini:
- Girdi: `date`, `stress`, `pam`, `socialLevel`, `phq4Q1..Q4`, `phq4Total`, `riskLevel`, `createdAt`
- ML cevabı (API başarısızsa null): `predictedRisk`, `finalIsim`, `finalRenk`,
  `guvenilirlik`, `top5Neden`, `aciklama`, `oneri`, `cascade`, `hibritMl`,
  `pasifMl`, `forecasting`, `mobilMeta`, `psikolojikAnaliz`
- Karmaşık alanlar `Converters` ile JSON olarak saklanır.

**`ham_pasif_kayit`** (`HamPasifKayit`) — ham pasif ölçüm:
- `timestamp`, `tip` (ör. `unlock_num`, `unlock_duration`, `act_still`,
  `loc_dist`, `loc_visit_num`...), `deger` (adet/saniye/metre)
- Epoch'a bölme/gönderme bu tablodan **sonra** yapılır; burada sadece ham birikim.

**`kullanicilar`** (`KullaniciEntity`) — kimlik:
- `ad`, `email` (**unique index**), `sifreHash` (SHA-256 hex — düz şifre saklanmaz)
- `yas`, `cinsiyet`, `onayTarihi` (onboarding'de toplanır, hepsi nullable)

### Migration zinciri (hepsi veri-koruyucu, destructive değil)

| Migration | Yapar |
|---|---|
| v3 → v4 | `ham_pasif_kayit` tablosunu ekler |
| v4 → v5 | `ema_entries`'e `forecasting` + `mobilMeta` kolonları |
| v5 → v6 | `kullanicilar` tablosu + e-posta unique index |
| v6 → v7 | `kullanicilar`'a `yas` + `cinsiyet` + `onayTarihi` kolonları |
| v7 → v8 | `ema_entries`'e `psikolojikAnaliz` (JSON) kolonu |

> **Migration kuralı:** En güncel sürüm v8. Eski migration'ları **ileride
> DEĞİŞTİRME** (bir cihaz o sürüme geçtikten sonra bozulur). Yeni alan
> gerekirse **v8→v9** aç.

### Kimlik akışı (`KimlikViewModel` + `KullaniciDao`)

- **Kayıt:** ad/e-posta/şifre doğrulanır → e-posta zaten kayıtlıysa reddedilir →
  şifre SHA-256'lanıp `kullanicilar`'a eklenir → otomatik giriş YOK, giriş ekranına döner.
- **Giriş:** boş alan reddedilir → e-posta Room'da yoksa veya şifre hash'i
  uyuşmazsa "E-posta veya şifre hatalı" → doğruysa ana ekrana geçer.

---

## 4. Pasif Veri Toplama

### Arka plan servisi (`PasifServisi` — foreground service)

- `START_STICKY`, kalıcı bildirim ("Ruh hali analizi için arka planda veri
  toplanıyor"), tip = `location|dataSync`.
- **Saatlik döngü:** her saat `PasifToplayici.usageTopla()` + `konumTopla()`.
- **Aktivite:** 60 sn aralıkla `ActivityRecognition` güncellemesi → `AktiviteAliciBR`
  broadcast receiver → Room'a yazar.
- İzin yoksa ilgili kaynak atlanır, servis çökmez.

### Toplayıcı (`PasifToplayici`)

- `usageTopla(bas, bit)` — `UsageStatsManager.queryEvents` ile o aralıkta
  unlock sayısı, ekran açık süresi, medya uygulaması açma/süre hesaplar; kayıt
  `timestamp = bit` (aralık sonu) damgasıyla Room'a yazılır.
- `konumTopla()` — anlık konum; önceki konuma göre mesafe (`loc_dist`) ve
  yeni "durak" (≥100 m) ise `loc_visit_num` yazar.
- `gecmisUsageBackfill(ctx, gunSayisi=7)` — **geçmiş backfill**: son 7 günün
  her biri için `usageTopla(günBaşı, günSonu)` çağırır. İlk `İzinler → DEVAM`'da
  `pasif_prefs/backfill_yapildi` flag'iyle **tek sefer** çalışır (idempotent).
  Sadece UsageStats geçmişi geri-doldurulur — aktivite/konum geçmişi Android'de
  yoktur, backend eksiğini medyanla doldurur.

### Epoch dönüşümü (`PasifEpoch`)

Backend'in beklediği `{tip}_ep_{n}` formatına çevirir:

- **Epoch (yerel saate göre):** `ep_1`=00–09, `ep_2`=09–18, `ep_3`=18–24,
  `ep_0`=tüm gün (her kayıt ayrıca ep_0'a da eklenir).
- Aggregation = **SUM**. Backend'in beklediği **30 feature** whitelist'i dışı
  gönderilmez. Verisi olmayan feature hiç eklenmez (0 değil — yok).
- `act_on_bicycle → act_on_bike` normalize edilir.
- `pasifVeriOlustur(gun)` → tek günün `Map<String, Double>`'ı.
- `pasifGunlukSeri(bugun, gunSayisi=7)` → son 7 günün **günlük** map listesi
  (`[{"gun": "...", <30 feature>}...]`). Telefonda rolling/lag **YOK** — backend
  bu listeden türetilmiş feature'ları eğitimle birebir aynı hesaplar.

---

## 5. Backend Bağlantısı (Python ML servisi)

### Retrofit konfigürasyonu (`RetrofitClient`)

- `BASE_URL` — gerçek telefonda bilgisayarın LAN IP'si (`http://<LAN-IP>:8000/`),
  emülatörde `http://10.0.2.2:8000/`. Her ortam değişiminde `RetrofitClient`'tan
  elle güncellenir.
- `usesCleartextTraffic="true"` (http'ye izin), 15 sn timeout'lar.
- `HttpLoggingInterceptor` Level.BODY → istek/cevap gövdesi Logcat'e düşer
  (doğrulama için kullanılıyor).

### Endpoint'ler (`MoodApiService`)

| Endpoint | Açıklama |
|---|---|
| `POST /predict/mobile` | EMA + pasif + 7 günlük seri (uygulamanın tek kullandığı) |

### İstek gövdesi (`TahminIstegi`)

```json
{
  "uid": "telefon_kullanici_001",
  "gun": "2026-05-18",
  "ema": { "stress":3, "pam_score":2, "social_level":3,
           "phq4_q1":0, "phq4_q2":0, "phq4_q3":0, "phq4_q4":0 },
  "pasif": { "act_still_ep_0": 29446.7, ... },          // tek gün (korundu)
  "pasif_gunluk": [ { "gun":"2026-05-16", ... },         // son ~7 gün serisi
                    { "gun":"2026-05-17", ... },
                    { "gun":"2026-05-18", ... } ]
}
```
`null` alanları Gson otomatik atlar (pasif veri yoksa `pasif`/`pasif_gunluk` gitmez).

### Cevap gövdesi (`TahminCevabi`)

- `cascade` — kural-tabanlı klinik tahmin (her zaman dolu)
- `pasif_ml` — pasif veri tek başına model
- `forecasting` — **yarınki riski** tahmin eden leakage'sız ana model (AUC ~0.99)
- `hibrit_ml` — leakage'lı, **hiçbir yerde gösterilmez**
- `final_risk` / `final_isim` / `final_renk` — birleştirilmiş nihai karar
- `guvenilirlik`, `aciklama`, `oneri`, `top_5_neden`
- `mobil_meta` — `telefondan_gelen_feature`, `medyanla_doldurulan_feature`,
  `toplam_pasif_feature`, **`mod`** (`"7-gunluk"` / `"tek-gun"`),
  **`pasif_gun_sayisi`** (seride kaç gün vardı)

### Backend ne yapıyor

1. EMA + pasif (tek gün) + `pasif_gunluk` serisini alır.
2. Seri doluysa rolling/lag/türetilmiş feature'ları **kendisi** hesaplar
   (`_lag1`, `_rmean7`, `_rstd7`...) → `mod = "7-gunluk"`. Seri boşsa tek-gün moduna düşer.
3. Telefondan gelmeyen feature'ları popülasyon medyanıyla doldurur.
4. 3 modeli (cascade / pasif_ml / forecasting) çalıştırıp birleştirir, sonucu döner.

> **Sunum gotcha'sı:** Gerçek rolling feature için ~7 günlük gerçek pasif veri
> gerekir. Emülatörde geçmiş UsageStats olmadığından `pasif_gun_sayisi` düşük
> çıkabilir — beklenen. Gerçek telefonda veri biriktikçe 5–7'ye çıkar.

### Sonuç ekranı / Raporlar — 3 katmanlı gösterim

1. 🤖 **Klinik (cascade)** — kural-tabanlı
2. 📡 **Telefon Davranış Analizi (pasif_ml)** — "7 günlük veri" rozeti
   (`mod == "7-gunluk"` ise), "Telefondan X ölçüm alındı, Y medyanla tamamlandı"
3. 🔮 **Yarın İçin Risk Tahmini (forecasting)** — hocaya gösterilecek asıl model

---

## 6. APK & Sunum Notları

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- Kurulum: `adb install -r <apk>` veya dosya transferi.
- Sunum öncesi: Python sunucusu `192.168.1.4:8000`'de açık, telefon aynı Wi-Fi'de.
- İlk açılışta backfill bir kez çalışır; sonraki açılışlarda flag yüzünden tekrar etmez.
