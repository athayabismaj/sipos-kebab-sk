# Sipos Kebab SK - Point of Sale & Inventory 🥙

[![id](https://img.shields.io/badge/lang-id-red.svg)](README-id.md)
[![en](https://img.shields.io/badge/lang-en-blue.svg)](README.md)

Aplikasi kasir (POS) dan manajemen inventaris modern yang dirancang khusus untuk bisnis Kebab. Dibangun menggunakan **Kotlin** dan **Jetpack Compose**, serta terintegrasi langsung dengan backend **Laravel REST API** (`siinv-kebab-sk`).

![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-2.11-48B983)
![API](https://img.shields.io/badge/API-Laravel-FF2D20?logo=laravel&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

## ✨ Fitur Utama

- **🛒 Kasir / POS**: Proses checkout yang cepat dengan kalkulasi otomatis, mendukung metode pembayaran Tunai & QRIS.
- **📦 Manajemen Menu & Varian**: Kelola produk, varian, harga jual, dan ketersediaan stok secara *real-time*.
- **🧾 Struk Digital**: Cetak struk fisik via **Printer Thermal Bluetooth** (ESC/POS) atau bagikan struk digital dalam bentuk teks.
- **📊 Riwayat Transaksi**: Laporan penjualan harian yang komprehensif beserta ringkasan pendapatan.
- **🏪 Manajemen Stok Harian**: Buka/tutup sesi stok harian, pencatatan bahan baku masuk, dan validasi resep otomatis.
- **💰 Pengeluaran Operasional**: Catat dan pantau pengeluaran operasional harian langsung dari aplikasi.
- **👤 Profil & Autentikasi**: Login aman (JWT/Token), manajemen profil, dan pembaruan kata sandi.
- **🔐 Lupa Password**: Alur reset kata sandi yang aman menggunakan OTP via Email.
- **🎨 UI/UX Premium**: Desain modern menggunakan Material 3, aset bertema kebab, *splash screen*, dan *micro-animations* yang mulus.

## 🏗️ Arsitektur

Proyek ini secara ketat mengimplementasikan prinsip **Clean Architecture** untuk memastikan skalabilitas, kemudahan pengujian (*testability*), dan pemisahan logika (*separation of concerns*).

```text
app/
├── common/              # Fungsi utilitas & helper
├── data/
│   └── network/         # NetworkModule (Retrofit + OkHttp)
├── feature/             # Modul fitur (Auth, Checkout, Menu, dll.)
│   ├── presentation/    # Layer UI: Jetpack Compose + ViewModels
│   ├── domain/          # Layer Domain: UseCases & Models
│   └── data/            # Layer Data: Repositories & API Services
└── ui/
    └── theme/           # Tema Material 3, Tipografi, Warna
```

## 🛠️ Tech Stack

| Kategori | Teknologi |
|---|---|
| **Bahasa** | Kotlin 2.0 |
| **Framework UI** | Jetpack Compose + Material 3 |
| **State Management** | ViewModel + StateFlow |
| **Networking** | Retrofit 2.11 + OkHttp 4.12 |
| **Serialisasi** | Gson |
| **Integrasi Perangkat** | Android Bluetooth API (ESC/POS Thermal Printers) |
| **Sistem Build** | Gradle 9.1 + Version Catalog (TOML) |
| **Integrasi Backend** | Laravel REST API (`siinv-kebab-sk`) |
| **Min SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 16 (API 36) |

## 📋 Prasyarat

Sebelum memulai, pastikan Anda telah memenuhi persyaratan berikut:
- **Android Studio** Ladybug (atau yang lebih baru).
- **JDK 11** (atau yang lebih baru).
- **Android SDK** API 36.
- Backend **Laravel REST API** (`siinv-kebab-sk`) harus sudah berjalan di lokal atau di-deploy ke server.

## 🚀 Memulai Proyek

### 1. Clone Repositori
```bash
git clone https://github.com/username/sipos-kebab-sk.git
cd sipos-kebab-sk
```

### 2. Konfigurasi API
Anda perlu menghubungkan aplikasi ke backend API. Buat atau edit file `local.properties` di *root* proyek dan tambahkan URL API Anda:

```properties
API_BASE_URL_DEBUG=http://ip-lokal-anda:8000/api/
API_BASE_URL_RELEASE=https://domain-produksi-anda.com/api/
```
*Catatan: Build release secara ketat mewajibkan penggunaan domain HTTPS.*

### 3. Build & Run
Anda dapat menjalankan aplikasi langsung melalui Android Studio atau menggunakan perintah Gradle:
```bash
# Build APK debug
./gradlew assembleDebug

# Install ke perangkat/emulator yang terhubung
./gradlew installDebug
```

## 🖨️ Pengaturan Printer Bluetooth

Aplikasi mendukung pencetakan struk menggunakan Printer Thermal Bluetooth standar (protokol ESC/POS):
1. Lakukan *pairing* printer thermal Anda di **Pengaturan Bluetooth** perangkat Android Anda.
2. Buka aplikasi dan navigasi ke **Profil** → **Printer Bluetooth**.
3. Pilih printer yang telah di-*pairing* dari daftar.
4. Konfigurasi akan disimpan secara otomatis untuk transaksi berikutnya.

## 🤝 Kontribusi

Kontribusi selalu diterima! Silakan ikuti langkah-langkah berikut:
1. *Fork* proyek ini.
2. Buat *branch* fitur Anda (`git checkout -b fitur/FiturKeren`).
3. *Commit* perubahan Anda (`git commit -m 'Menambahkan FiturKeren'`).
4. *Push* ke *branch* tersebut (`git push origin fitur/FiturKeren`).
5. Buat *Pull Request*.

## 📄 Lisensi

Didistribusikan di bawah Lisensi MIT. Lihat `LICENSE` untuk informasi lebih lanjut.
