# SIPOS Kebab SK

Aplikasi Android untuk pengelolaan operasional usaha kebab (SIPOS), dibangun dengan **Kotlin** dan **Jetpack Compose**.

## Ringkasan
Proyek ini menyiapkan fondasi aplikasi kasir/operasional yang bisa dikembangkan untuk kebutuhan:
- Transaksi penjualan
- Manajemen menu
- Rekap laporan sederhana
- Alur kerja kasir berbasis mobile

Saat ini proyek masih pada tahap awal (starter app) dan siap dilanjutkan ke fitur bisnis inti.

## Teknologi
- Kotlin
- Android SDK
- Jetpack Compose (Material 3)
- Gradle Kotlin DSL

## Struktur Proyek
- `app/` - Source code aplikasi Android
- `gradle/` - Konfigurasi Gradle wrapper dan version catalog
- `build.gradle.kts` - Konfigurasi build level root
- `settings.gradle.kts` - Konfigurasi modul proyek

## Cara Menjalankan
1. Buka proyek di Android Studio (versi terbaru disarankan).
2. Pastikan Android SDK sudah terpasang.
3. Sync Gradle.
4. Jalankan aplikasi ke emulator/perangkat Android.

Atau via terminal:

```bash
./gradlew assembleDebug
```

Untuk Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Status Pengembangan
Current status: **Bootstrap / Initial Setup**

Contoh tampilan awal masih berupa halaman default Compose (`Hello Android`).

## Rencana Pengembangan
- [ ] Halaman daftar menu kebab
- [ ] Keranjang & checkout
- [ ] Riwayat transaksi harian
- [ ] Penyimpanan lokal (Room)
- [ ] Integrasi backend (opsional)

## Kontribusi
Kontribusi terbuka melalui issue dan pull request.

## Lisensi
Belum ditentukan.

---

Dikembangkan untuk kebutuhan proyek **sipos-kebab-sk**.
