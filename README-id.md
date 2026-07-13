# SIPOS Kebab SK

Aplikasi mobile Point of Sale (POS) dan manajemen inventaris untuk operasional Kebab SK.

Project ini terdiri dari:
- Aplikasi mobile Android (Kotlin + Jetpack Compose)
- Integrasi dengan REST API Laravel (SIINV Kebab SK)

## Fitur Utama

### Kasir / POS
- Checkout cepat dengan kalkulasi otomatis
- Mendukung metode pembayaran Tunai dan QRIS
- Cetak struk digital (Printer thermal Bluetooth & share teks)

### Inventaris & Stok
- Manajemen stok harian (buka/tutup sesi)
- Pencatatan bahan masuk
- Validasi resep otomatis
- Manajemen menu, varian, dan harga

### Operasional & Laporan
- Pencatatan pengeluaran operasional harian
- Riwayat transaksi komprehensif
- Laporan penjualan dan ringkasan pendapatan harian

### Autentikasi & Profil
- Login aman dengan token API
- Manajemen profil dan kata sandi
- Lupa password via OTP Email

## Teknologi
- Kotlin 2.0
- Jetpack Compose + Material 3
- ViewModel + StateFlow
- Retrofit 2.11 + OkHttp 4.12
- Android Bluetooth API (ESC/POS)
- Gradle 9.1

## Struktur Penting
- `app/src/main/java/com/sipos/kebabsk/feature` -> Modul fitur (auth, checkout, menu, dll.)
- `app/src/main/java/com/sipos/kebabsk/data/network` -> Modul network dan konfigurasi API
- `app/src/main/java/com/sipos/kebabsk/ui/theme` -> Styling UI dan tema Material 3
- `build.gradle.kts` -> Dependensi dan konfigurasi
- `local.properties` -> Variabel environment lokal (URL API)

## Instalasi Lokal

1. Clone repository
```bash
git clone https://github.com/athayabismaj/sipos-kebab-sk.git
cd sipos-kebab-sk
```

2. Konfigurasi API
Buat file `local.properties` di folder root dan atur URL API:
```properties
API_BASE_URL_DEBUG=http://ip-lokal-anda:8000/api/
API_BASE_URL_RELEASE=https://domain-anda.com/api/
```

3. Build & Run
```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Catatan Penting

1. Pastikan backend `siinv-kebab-sk` sudah berjalan dan dapat diakses dari jaringan Anda.
2. Untuk cetak struk, lakukan *pairing* printer thermal Bluetooth terlebih dahulu di pengaturan Android sebelum memilihnya di menu Profil aplikasi.
3. Build release mewajibkan penggunaan domain HTTPS pada `API_BASE_URL_RELEASE`.
