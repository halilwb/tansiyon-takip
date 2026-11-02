# Tansiyon Takip

Sabri Baba için tasarlanmış, sade ve şık bir tansiyon takip uygulaması.

**Renk paleti:** beyaz + lacivert tonları, vurgu rengi sarı (kenarlıklar, rozetler ve grafik detaylarında).

## Özellikler

- Sol üstte **“Hoşgeldin Sabri Baba”** karşılaması
- Saat, tarih, büyük (sistolik) ve küçük (diyastolik) tansiyon girişi
- 30 günlük gerçekçi örnek veri (ilk açılışta otomatik oluşturulur)
- Büyük/küçük tansiyon için **yükseliş–düşüş çizgi grafiği** (7 / 14 / 30 gün)
- Son ölçüm kartı, değişim oku (▲ / ▼), 7 günlük ortalama ve durum rozeti
- Günlere göre gruplanmış ölçüm geçmişi ve tek dokunuşla silme
- Tüm veriler cihazda saklanır; **hiçbir hassas izin veya internet bağlantısı gerekmez**

## APK indirme

📥 **[TansiyonTakip.apk — en son sürümü indir](https://github.com/halilwb/tansiyon-takip/releases/latest/download/TansiyonTakip.apk)**

Sürüm notları: [Releases](https://github.com/halilwb/tansiyon-takip/releases)

Kurulum: APK'yı telefona indirip aç. Android, bilinmeyen kaynaklardan uygulama yükleme iznini isteyebilir.

## APK'yı kendin oluşturma

GitHub Actions her `main` push'unda otomatik olarak imzalı APK üretir.

Yerel derleme için:

```bash
./gradlew assembleRelease
```

Çıktı: `app/build/outputs/apk/release/app-release.apk`

Gereksinimler: JDK 17, Android SDK 34.

## İzinler

Uygulama **hiçbir Android izni istemez**. Kamera, konum, kişiler, internet vb. kullanılmaz.
Veriler yalnızca uygulamanın kendi özel deposunda (`SharedPreferences`) tutulur.

## Yasal not

Bu uygulama yalnızca kişisel takip amaçlıdır. Ölçümler tıbbi tanı yerine geçmez.
