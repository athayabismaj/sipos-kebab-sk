# Kebab SK — Point of Sale & Inventory 🥙

Aplikasi kasir (POS) dan manajemen inventaris modern untuk bisnis kebab, dibangun dengan **Kotlin + Jetpack Compose** dan terhubung ke backend **Laravel REST API**.

![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-2.11-48B983)
![API](https://img.shields.io/badge/API-Laravel-FF2D20?logo=laravel&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## ✨ Fitur

- 🛒 **Kasir / POS** — Checkout cepat dengan kalkulasi otomatis, metode pembayaran tunai & QRIS
- 📦 **Manajemen Menu & Varian** — Kelola menu, varian, harga jual, dan ketersediaan stok
- 🧾 **Struk Digital** — Cetak struk via **Bluetooth Thermal Printer** atau bagikan sebagai teks
- 📊 **Riwayat Transaksi** — Laporan penjualan harian dengan ringkasan pendapatan
- 🏪 **Stok Harian** — Buka/tutup sesi stok, input bahan masuk, validasi resep otomatis
- 💰 **Pengeluaran Operasional** — Catat pengeluaran harian langsung dari aplikasi
- 👤 **Profil & Autentikasi** — Login aman dengan token API, ubah profil & password
- 🔐 **Lupa Password** — Reset password via kode OTP email
- 🎨 **UI Premium** — Desain modern dengan tema kebab, splash screen, dan micro-animations

---

## 🏗️ Arsitektur

```
app/
├── common/              # Utility & helper functions
├── data/
│   └── network/         # NetworkModule (Retrofit + OkHttp)
├── feature/
│   ├── auth/            # Login, Register, Forgot Password
│   ├── checkout/        # Proses pembayaran & struk
│   ├── dailystock/      # Sesi stok harian
│   ├── expense/         # Pengeluaran operasional
│   ├── inventory/       # Manajemen inventaris
│   ├── menu/            # Daftar menu, varian, keranjang
│   ├── profile/         # Profil user, Bluetooth printer
│   ├── shift/           # Manajemen shift kasir
│   ├── splash/          # Splash screen
│   └── transactions/    # Riwayat transaksi & laporan
└── ui/
    └── theme/           # Warna, tipografi, tema Material3
```

Setiap feature mengikuti pola **Clean Architecture** dengan layer:

```
presentation/ → domain/ → data/
(ViewModel + Compose UI)  (Model & UseCase)  (Repository + API Service)
```

---

## 🛠️ Tech Stack

| Layer | Teknologi |
|---|---|
| **Language** | Kotlin 2.0 |
| **UI Framework** | Jetpack Compose + Material 3 |
| **State Management** | ViewModel + StateFlow |
| **Networking** | Retrofit 2.11 + OkHttp 4.12 |
| **Serialization** | Gson |
| **Bluetooth** | Android Bluetooth API (ESC/POS) |
| **Build System** | Gradle 9.1 + Version Catalog (TOML) |
| **Backend** | Laravel REST API |
| **Min SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 16 (API 36) |

---

## 📋 Prasyarat

- **Android Studio** Ladybug atau lebih baru
- **JDK 11** atau lebih baru
- **Android SDK** API 36
- Backend Laravel REST API sudah berjalan (production atau lokal)

---

## 🚀 Instalasi & Menjalankan

```bash
# Clone repository
git clone https://github.com/username/sipos-kebab-sk.git

cd sipos-kebab-sk
```

### Konfigurasi API

URL API dikonfigurasi di `app/build.gradle.kts`:

```kotlin
buildTypes {
    release {
        buildConfigField("String", "API_BASE_URL", "\"https://your-domain.com/api/\"")
    }
    debug {
        buildConfigField("String", "API_BASE_URL", "\"http://your-local-ip:8000/api/\"")
    }
}
```

> Sesuaikan URL dengan alamat server backend Anda.

### Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (minified + optimized)
./gradlew assembleRelease

# Install ke device yang terhubung
./gradlew installDebug
```

Atau langsung tekan ▶️ **Run** di Android Studio.

---

## 📦 Perintah yang Tersedia

| Perintah | Deskripsi |
|---|---|
| `./gradlew assembleDebug` | Build APK debug |
| `./gradlew assembleRelease` | Build APK release (minified) |
| `./gradlew installDebug` | Install langsung ke device |
| `./gradlew test` | Jalankan unit test |
| `./gradlew connectedAndroidTest` | Jalankan instrumented test |
| `./gradlew lint` | Jalankan Android Lint |
| `./gradlew clean` | Bersihkan build cache |

---

## 🖨️ Konfigurasi Printer Bluetooth

Aplikasi mendukung cetak struk via printer thermal Bluetooth (ESC/POS):

1. Pair printer di **Pengaturan Bluetooth** HP
2. Buka menu **Profil** → **Printer Bluetooth**
3. Pilih printer dari daftar perangkat
4. Printer akan tersimpan untuk sesi berikutnya


---

## 📁 Struktur Proyek

```
sipos-kebab-sk/
├── app/
│   ├── build.gradle.kts          # Konfigurasi app module
│   ├── proguard-rules.pro        # Aturan R8/ProGuard
│   └── src/main/
│       ├── AndroidManifest.xml   # Permissions & activity
│       ├── java/.../kebabsk/     # Source code Kotlin
│       └── res/                  # Resources (drawable, values, xml)
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── build.gradle.kts              # Konfigurasi root project
├── gradle.properties             # Gradle JVM & Android settings
└── settings.gradle.kts           # Module settings
```

---

## 🤝 Kontribusi

1. Fork repository ini
2. Buat branch fitur (`git checkout -b fitur/fitur-baru`)
3. Commit perubahan (`git commit -m 'Tambah fitur baru'`)
4. Push ke branch (`git push origin fitur/fitur-baru`)
5. Buat Pull Request

---

## 📄 Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

---

<p align="center">
  Dibuat dengan ❤️ untuk <strong>Kebab SK</strong>
</p>
