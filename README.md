<div align="center">
  <img src="https://raw.githubusercontent.com/android/architecture-samples/main/app/src/main/ic_launcher-web.png" alt="Android App Logo" width="100" />
  <h1>Kebab SK - SIPOS</h1>
  <p><b>Aplikasi Mobile Kasir (Point of Sales) & Inventaris Berbasis Android</b></p>
  
  [![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
  [![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=flat-square)](https://square.github.io/retrofit/)
  
  <br>
  
  <a href="README.md"><img src="https://img.shields.io/badge/-ID-E11D48?style=for-the-badge" alt="ID" /></a>
  &nbsp;&nbsp;
  <a href="README-en.md"><img src="https://img.shields.io/badge/-ENG-1E40AF?style=for-the-badge" alt="ENG" /></a>
</div>

---

## 📖 Tentang Proyek
**SIPOS (Sistem Point of Sales)** adalah aplikasi *mobile* kasir dan manajemen inventaris yang terintegrasi langsung dengan backend SIINV Kebab SK. Aplikasi ini dibangun dengan standar arsitektur modern Android (Clean Architecture) menggunakan Kotlin dan Jetpack Compose. SIPOS dirancang untuk mempercepat proses transaksi harian, mencetak struk secara digital maupun fisik, dan memvalidasi ketersediaan stok secara *real-time*.

---

## ✨ Fitur Utama

### 🛒 Kasir / POS
- 🚀 **Transaksi Cepat:** Pemrosesan *checkout* dinamis dengan kalkulasi harga otomatis.
- 💳 **Metode Pembayaran:** Dukungan multi-pembayaran termasuk Tunai dan QRIS.
- 🧾 **Struk Digital:** Fitur cetak struk via **Printer Thermal Bluetooth** (ESC/POS) atau bagikan langsung sebagai teks.

### 📦 Inventaris & Stok Harian
- 🏪 **Sesi Operasional:** Manajemen buka/tutup sesi stok harian toko.
- 📋 **Input Stok Masuk:** Pencatatan bahan baku (roti, daging, sayur) yang diterima dari pusat.
- 🔄 **Validasi Otomatis:** Pengecekan otomatis ketersediaan bahan berdasarkan resep saat transaksi.

### 📊 Laporan & Operasional
- 💰 **Pengeluaran Toko:** Pencatatan pengeluaran operasional harian secara langsung dari aplikasi.
- 📈 **Riwayat Transaksi:** Pantau ringkasan omzet dan riwayat penjualan harian kasir.

### 🔐 Autentikasi & Keamanan
- 🔑 **Akses Aman:** Login dengan *Bearer Token* API.
- 🛡️ **Pemulihan Akun:** Fitur ganti kata sandi dan *Forgot Password* dengan verifikasi OTP via Email.

---

## 🛠️ Arsitektur & Teknologi
Proyek ini mengimplementasikan **Clean Architecture** (Presentation, Domain, Data) untuk menjaga kode tetap rapi dan mudah diuji.
- **Bahasa:** Kotlin 2.0
- **UI Framework:** Jetpack Compose + Material 3
- **State Management:** ViewModel + StateFlow
- **Networking:** Retrofit 2.11 + OkHttp 4.12 + Gson
- **Hardware Integrasi:** Android Bluetooth API (ESC/POS)
- **Minimum SDK:** API 26 (Android 8.0) | **Target SDK:** API 36 (Android 16)

---

## 🚀 Memulai (Instalasi Lokal)

Untuk menjalankan proyek Android ini di mesin lokal, ikuti langkah berikut:

1. **Kloning Repositori**
   ```bash
   git clone https://github.com/athayabismaj/sipos-kebab-sk.git
   cd sipos-kebab-sk
   ```
2. **Konfigurasi URL API**
   Aplikasi membutuhkan koneksi ke backend SIINV. Buat berkas `local.properties` di folder *root* proyek, lalu tambahkan konfigurasi berikut:
   ```properties
   API_BASE_URL_DEBUG=http://ip-lokal-anda:8000/api/
   API_BASE_URL_RELEASE=https://domain-produksi-anda.com/api/
   ```
   *(Ganti `ip-lokal-anda` dengan IP dari server SIINV yang sedang berjalan).*
3. **Build & Run Aplikasi**
   Buka proyek ini menggunakan **Android Studio (Ladybug atau yang lebih baru)**.
   Atau jalankan perintah Gradle berikut di terminal:
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

---

## 🖨️ Panduan Printer Bluetooth

Aplikasi mendukung pencetakan langsung melalui *printer thermal*:
1. Buka **Pengaturan Bluetooth** pada perangkat Android Anda dan *pair* printer thermal.
2. Buka aplikasi SIPOS, masuk ke menu **Profil** -> **Printer Bluetooth**.
3. Pilih printer Anda dari daftar yang tersedia. Konfigurasi ini akan otomatis tersimpan.

---
<br />
<div align="center">
  <sub>Hak Cipta Terpelihara. Dibangun untuk kelancaran operasional <b>Kebab SK</b> &copy; 2026.</sub>
</div>
