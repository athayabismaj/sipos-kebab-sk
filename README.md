<div align="center">
  <img src="app/src/main/res/drawable-nodpi/kebab_sk_logo.png" alt="Logo Kebab SK" width="96" />
  <h1>Kebab SK — SIPOS</h1>
  <p><strong>Aplikasi kasir Android yang terintegrasi dengan stok dan operasional SIINV.</strong></p>

  [![Android API 26+](https://img.shields.io/badge/Android-API_26%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
  [![Kotlin 2.0.21](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
  [![AGP 9.0.1](https://img.shields.io/badge/AGP-9.0.1-3DDC84?style=flat-square&logo=androidstudio&logoColor=white)](https://developer.android.com/build/releases/gradle-plugin)

  <br><br>

  <a href="README.md"><img src="https://img.shields.io/badge/Bahasa-Indonesia-E11D48?style=for-the-badge" alt="Bahasa Indonesia" /></a>
  <a href="README-en.md"><img src="https://img.shields.io/badge/Language-English-1E40AF?style=for-the-badge" alt="English" /></a>
</div>

---

## Edisi Publik

Repositori ini adalah **edisi publik terakhir** SIPOS Kebab SK: sebuah snapshot stabil yang ditujukan untuk dokumentasi teknis, portofolio, evaluasi, dan demonstrasi arsitektur aplikasi kasir Android.

Pengembangan berikutnya—termasuk konfigurasi produksi, kredensial penandatanganan, integrasi khusus, perbaikan internal, dan fitur baru—dapat dikelola secara privat dan tidak selalu diterbitkan kembali ke repositori ini.

SIPOS merupakan aplikasi klien dan membutuhkan backend SIINV yang kompatibel. Kredensial produksi, akun kasir, URL layanan internal, berkas penandatanganan, serta data operasional tidak menjadi bagian dari distribusi publik.

## Tentang SIPOS

SIPOS adalah aplikasi *Point of Sale* berbasis Android untuk kasir Kebab SK. Aplikasi menghubungkan autentikasi kasir, sesi stok harian, katalog menu, transaksi tunai, struk, riwayat transaksi, pengeluaran operasional, dan printer Bluetooth dengan REST API SIINV.

Alur utamanya:

1. Kasir masuk menggunakan akun yang memiliki peran kasir.
2. Aplikasi mengambil profil, status sesi harian, katalog menu, ketersediaan stok, dan ringkasan penjualan dari backend.
3. Kasir memilih varian menu dan memeriksa pesanan di keranjang.
4. Pembayaran tunai dikirim ke backend; harga, sesi, cabang, dan ketersediaan bahan divalidasi oleh server.
5. Transaksi yang berhasil menghasilkan struk digital yang dapat dibagikan atau dicetak melalui printer thermal Bluetooth.
6. Riwayat transaksi, stok harian, penutupan sesi, dan pengeluaran operasional tetap tersinkron dengan SIINV.

## Fitur Utama

### Kasir dan transaksi

- Dashboard kasir dengan status sesi harian, jumlah transaksi, item terjual, dan pendapatan hari berjalan.
- Katalog menu berdasarkan kategori dengan gambar varian, harga, dan status ketersediaan berbasis stok.
- Keranjang dengan perubahan kuantitas, penghapusan item, dan perhitungan total otomatis.
- Pembayaran **tunai** dengan pilihan nominal cepat, validasi uang diterima, perhitungan kembalian, dan pencegahan pengiriman ganda.
- Struk transaksi yang dapat ditampilkan, dibagikan sebagai teks, dan dicetak melalui printer thermal Bluetooth.
- *Pull-to-refresh* pada dashboard, katalog menu, riwayat transaksi, stok harian, dan profil.

### Riwayat dan pembatalan transaksi

- Filter riwayat berdasarkan tanggal, ringkasan transaksi, omzet, status lunas, dan status dibatalkan.
- Detail transaksi dan pencetakan ulang struk.
- Pembatalan transaksi hari berjalan sesuai sesi, kepemilikan transaksi, hak akses, dan validasi backend.
- Pilihan alasan pembatalan `restock` atau `waste` dikirim ke backend. Dampak akhirnya terhadap stok mengikuti kontrak dan implementasi backend yang digunakan.

### Stok dan operasional harian

- Melihat saldo serta status bahan pada sesi stok harian yang dibuka oleh admin.
- Memasukkan sisa fisik bahan dan menutup sesi harian.
- Mencatat pengeluaran operasional kasir dengan validasi nominal dan kategori.
- Menjaga konteks cabang berdasarkan sesi dan penugasan akun dari backend.

### Akun dan perangkat

- Login token, pemulihan kata sandi melalui OTP email, perubahan profil, perubahan kata sandi, dan logout.
- Penyimpanan sesi terenkripsi menggunakan Android Keystore.
- Pemilihan serta penyimpanan printer Bluetooth yang digunakan kasir.
- Pemisahan state berdasarkan sesi agar data akun sebelumnya tidak terbawa setelah pergantian pengguna.

## Arsitektur dan Teknologi

Kode disusun berdasarkan fitur dengan pemisahan lapisan presentation, domain, dan data. UI menggunakan pola MVVM dengan aliran state reaktif.

| Bagian | Teknologi |
|---|---|
| Bahasa | Kotlin 2.0.21 |
| UI | Jetpack Compose, Material 3 |
| State | ViewModel, StateFlow, Kotlin Coroutines |
| Dependency injection | Koin 3.5.6 |
| Navigasi | Navigation Compose 2.8.3 |
| API | Retrofit 2.11, OkHttp 4.12, Gson |
| Gambar | Coil Compose 2.7 |
| Printer | Android Bluetooth API, ESC/POS |
| Build | Android Gradle Plugin 9.0.1, Gradle 9.1.0 |
| Android | Minimum API 26, compile/target API 36 |
| Test | JUnit, coroutine test, Compose UI test |

Struktur utama:

```text
app/src/main/java/com/sipos/kebabsk/
├── common/        # utilitas, sesi, validasi, dan komponen bersama
├── di/            # konfigurasi dependency injection
├── feature/       # auth, menu, cart, checkout, stok, transaksi, dan profil
└── ui/theme/      # warna, tipografi, dan tema Compose
```

## Persyaratan Lokal

- Android Studio **Quail 3 (2026.1.3) Stable** atau versi lain yang mendukung AGP 9.0.1.
- Gradle JDK 17 atau lebih baru. JDK bawaan Android Studio dapat digunakan sehingga Java tidak harus dipasang terpisah.
- Android SDK Platform 36 dan SDK Build Tools 36.0.0.
- Perangkat atau emulator Android 8.0 (API 26) atau lebih baru.
- Backend [SIINV Kebab SK](https://github.com/athayabismaj/siinv-kebab-sk) yang dapat dijangkau dari perangkat.

## Instalasi Lokal

1. Kloning repositori dan masuk ke direktori proyek.

   ```bash
   git clone https://github.com/athayabismaj/sipos-kebab-sk.git
   cd sipos-kebab-sk
   ```

2. Buka proyek menggunakan Android Studio dan tunggu proses Gradle Sync selesai.

3. Buat atau lengkapi `local.properties` pada direktori root proyek. Android Studio biasanya menambahkan `sdk.dir` secara otomatis.

   ```properties
   API_BASE_URL_DEBUG=http://ip-server-lokal:8000/api/
   API_BASE_URL_RELEASE=https://your-domain.com/api/
   ```

   URL harus diakhiri dengan `/api/`. Jangan commit `local.properties` karena berisi konfigurasi khusus mesin dan dapat memuat alamat layanan internal.

4. Pilih alamat debug sesuai perangkat yang digunakan.

   - Emulator Android pada komputer backend: gunakan `http://10.0.2.2:8000/api/`.
   - Perangkat fisik: gunakan alamat IPv4 LAN komputer backend, pastikan keduanya berada pada jaringan yang sama, dan izinkan port server pada firewall.
   - Backend lokal harus mendengarkan antarmuka jaringan yang dapat dijangkau perangkat, bukan hanya `127.0.0.1`.

5. Jalankan aplikasi dari Android Studio atau melalui Gradle Wrapper.

   Windows PowerShell:

   ```powershell
   .\gradlew.bat assembleDebug
   .\gradlew.bat installDebug
   ```

   Linux/macOS:

   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## Build dan Pengujian

Windows PowerShell:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```

Linux/macOS:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
```

Snapshot publik terakhir telah diverifikasi dengan build debug dan **201 unit test** tanpa kegagalan. Pengujian dengan backend nyata, akun uji, transaksi, dan printer Bluetooth tetap perlu dilakukan pada lingkungan staging atau demonstrasi yang digunakan.

Build release hanya menerima `API_BASE_URL_RELEASE` berbasis HTTPS serta menolak placeholder bawaan dan host lokal yang dilarang secara eksplisit oleh konfigurasi Gradle. Konfigurasi keystore serta kredensial penandatanganan release harus disediakan secara privat oleh pihak yang melakukan deployment.

## Printer Bluetooth

1. Pasangkan printer thermal melalui pengaturan Bluetooth Android.
2. Buka SIPOS dan masuk ke **Profil → Printer Bluetooth**.
3. Berikan izin Bluetooth ketika diminta, lalu pilih printer yang telah dipasangkan.
4. Uji pencetakan melalui struk transaksi berhasil atau detail transaksi pada halaman riwayat.

Gunakan perangkat Android fisik untuk pengujian printer. Dukungan perintah ESC/POS dapat berbeda antarprodusen, sehingga lebar kertas, encoding, dan hasil cetak perlu diverifikasi pada perangkat target.

## Keamanan Publikasi

- Jangan commit `local.properties`, token API, kata sandi, OTP, URL layanan privat, keystore, atau kredensial penandatanganan.
- Token sesi disimpan menggunakan enkripsi berbasis Android Keystore dan dikecualikan dari backup aplikasi.
- Build release menonaktifkan *cleartext traffic*, logging jaringan debug, dan mode debug; minifikasi serta penyusutan resource diaktifkan.
- Harga, ketersediaan stok, sesi aktif, cabang, dan otorisasi transaksi harus tetap divalidasi oleh backend; data dari aplikasi klien tidak boleh menjadi sumber kebenaran tunggal.
- Laporkan celah keamanan secara privat kepada pemilik proyek dan jangan mempublikasikan kredensial atau data eksploitasi melalui issue publik.

## Batasan Edisi Publik

- Aplikasi membutuhkan backend SIINV dan akun kasir yang valid; repositori Android tidak menyediakan data operasional mandiri.
- Alur pembayaran yang diimplementasikan pada snapshot ini adalah pembayaran tunai. QRIS dan metode nontunai tidak diklaim tersedia.
- Pembukaan sesi dan distribusi stok dilakukan oleh admin melalui SIINV; aplikasi kasir berfokus pada pemantauan serta penutupan sesi.
- Perilaku pembatalan terhadap stok ditentukan backend dan harus diuji terhadap versi SIINV yang dipasangkan.
- Uji integrasi langsung untuk email OTP, perangkat Bluetooth, dan skenario staging memerlukan layanan eksternal serta data uji yang tidak disertakan.
- APK production, keystore, kredensial, dan konfigurasi deployment tidak disediakan pada repositori publik.

## Dokumentasi Teknis

- [Integrasi API](docs/API_INTEGRATION.md)
- [Pengujian kontrak Android](docs/ANDROID_CONTRACT_TESTING.md)
- [Fixture kontrak API](docs/API_CONTRACT_FIXTURES.md)
- [Skenario UAT mobile kasir](docs/UAT_E2E_MOBILE_KASIR.md)
- [Checklist keamanan deployment](docs/security-deployment-checklist.md)

## Dukungan dan Kontribusi

Repositori ini berfungsi terutama sebagai rilis publik dan referensi teknis. Permintaan fitur, roadmap, dukungan deployment, dan perubahan khusus operasional tidak dijamin tersedia pada edisi publik. Pull request dapat ditinjau, tetapi penerimaan dan jadwal rilis mengikuti kebijakan pemilik proyek.

## Hak Penggunaan

Repositori ini belum menyertakan berkas `LICENSE` khusus SIPOS. Hubungi pemilik proyek untuk izin penggunaan ulang, modifikasi, distribusi, atau pemakaian komersial. Framework, library, dan dependensi pihak ketiga tetap mengikuti lisensinya masing-masing.

Hak cipta © 2026 Kebab SK. Seluruh hak dilindungi.
