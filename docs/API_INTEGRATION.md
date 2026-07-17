# Integrasi API Android SIPOS Kebab SK

Backend Laravel `siinv-kebab-sk` adalah sumber kebenaran kontrak. Matriks lengkap
berada pada `docs/MOBILE_API_CONTRACT_MATRIX.md` di repository backend.

## Base URL

Base URL dibaca dari `local.properties` dan selalu dinormalisasi dengan trailing
slash:

```properties
API_BASE_URL_DEBUG=http://alamat-lokal:8000/api/
API_BASE_URL_RELEASE=https://domain-production/api/
```

Debug boleh memakai HTTP jaringan lokal. Build release wajib HTTPS dan Gradle
menolak placeholder, localhost, `127.0.0.1`, serta `10.0.2.2`. Jangan menyimpan
credential atau token di file konfigurasi.

## Authentication dan session lokal

- Login: `POST auth/login` dengan `username` dan `password`.
- Request terproteksi memakai `Authorization: Bearer <token>`.
- Token, profil, role, dan branch disimpan terenkripsi memakai Android Keystore.
- Respons `401` pada request terproteksi membersihkan session lokal.
- Logout memanggil `POST auth/logout`, lalu membersihkan token, profil, branch,
  dan state lokal. Kegagalan jaringan tetap membersihkan state device, tetapi
  revocation server baru pasti setelah request berhasil atau token kedaluwarsa.

## Branch

Backend mengambil cabang dari user pemilik token. Android menyimpan branch dari
login/profil untuk state dan tampilan, tetapi tidak mengirim `branch_id` pada
checkout, stok, transaksi, atau pengeluaran. Branch nullable ditangani tanpa
default hard-coded. Logout menghapus branch tersimpan.

## Endpoint Retrofit aktif

| Fitur | Method | Path |
| --- | --- | --- |
| Login | POST | `auth/login` |
| Profil | GET | `auth/me` |
| Logout | POST | `auth/logout` |
| Menu | GET | `menus` |
| Metode bayar | GET | `payment-methods` |
| Status sesi | GET | `sessions/current-status` |
| Stok harian | GET | `daily-stock-items` |
| Tutup sesi | POST | `daily-stock-sessions/close` |
| Checkout | POST | `transactions` |
| Riwayat | GET | `transactions` |
| Detail | GET | `transactions/{reference}` |
| Receipt | GET | `transactions/{reference}/receipt` |
| Void | POST | `transactions/{id}/void` |
| Ringkasan omzet | GET | `revenue/summary` |
| Tren omzet | GET | `revenue/trend` |
| Pengeluaran | POST | `cashflow/expenses` |

Endpoint lama `sessions/{id}/close` tidak boleh digunakan kembali.

## Daily Stock Session

Admin membuka sesi melalui web. Android membaca status dengan
`GET sessions/current-status`, memuat item melalui `GET daily-stock-items`, dan
menutupnya melalui `POST daily-stock-sessions/close`.

- `200`: status sesi terbaca.
- `404`: tidak ada sesi aktif; tampilkan "Sesi Harian Belum Dibuka".
- Network/timeout/JSON rusak: status unknown; checkout tetap terkunci sampai
  refresh berhasil.
- `409`: konflik state bisnis; tampilkan pesan backend yang sudah disanitasi.

## Checkout

Android mengirim `payment_method_id`, `paid_amount`, daftar `variant_id` dan
`qty`, serta `note` opsional. Android tidak mengirim harga atau branch. Backend
menjadi penentu total, stok, kembalian, dan kode transaksi. Double-submit dijaga
di ViewModel; request tidak di-retry otomatis.

## Transactions, receipt, dan pagination

Referensi detail dapat berupa ID atau kode transaksi. Paginator riwayat dibaca
dari envelope custom backend: `data.data`, `current_page`, `last_page`,
`per_page`, dan `total`. Receipt mengizinkan field opsional/null dan nominal
dipetakan ke Kotlin `Long`.

## Error handling

| Kondisi | Pesan/perilaku client |
| --- | --- |
| 400 | Request tidak valid. |
| 401 | Session dibersihkan dan user login ulang. |
| 403 | Akses tidak diizinkan. |
| 404 | Pesan resource terkait; current-status berarti sesi belum dibuka. |
| 409 | Pesan konflik bisnis dari backend dipertahankan bila aman. |
| 422 | Pesan validasi ditampilkan setelah sanitasi. |
| 429 | User diminta mencoba beberapa saat lagi. |
| 500-599 | Gangguan layanan tanpa detail internal. |
| Network | User diminta memeriksa koneksi. |
| Timeout | User diminta mencoba lagi. |
| JSON rusak | Respons server tidak dapat dibaca. |

Header Authorization disensor pada logging. Password, token, dan receipt lengkap
tidak boleh dicatat di log release.

## Contract fixtures dan test

Fixture berada di `app/src/test/resources/contracts`. Sumbernya adalah response
fiktif dari contract test backend, bukan data production. Perlindungan mencakup:

- anotasi method/path Retrofit;
- login, profil, session persistence, dan logout;
- menu/variant dan status sesi unknown;
- current-status aktif, tidak aktif, dan network failure;
- stok harian dan tutup sesi;
- serialisasi checkout dan parsing response;
- transaksi, pagination, detail, void, dan receipt;
- pemetaan HTTP/network/timeout/JSON rusak.

Pengujian unit tidak menggantikan smoke test device. Sebelum release, jalankan
login, katalog, status sesi, checkout data testing, riwayat, receipt, stok, dan
logout pada emulator/device yang terhubung ke backend development.

## Hasil verifikasi 17 Juli 2026

- Android unit test run 1 dan run 2: 193 test, 0 gagal, 0 skipped.
- Android lint: lulus.
- `assembleDebug`: lulus untuk universal dan seluruh ABI yang dikonfigurasi.
- `assembleRelease`: lulus sebagai APK unsigned; signing production tidak diubah.
- Backend Laravel: 229 test, 1.562 assertion, 0 gagal.
- PostgreSQL concurrency run 1 dan 2: 11 test, 61 assertion, 0 gagal.
- Smoke emulator: APK terpasang dan cold-start; profil/dashboard serta riwayat
  dapat dimuat; current-status 404 tampil sebagai sesi belum dibuka; tidak ada
  crash, 401, route 404, atau JSON parse error dari aplikasi di Logcat.
- Checkout, receipt, dan logout-login ulang pada device belum dijalankan karena
  sesi testing hari itu belum dibuka dan tidak ada transaksi testing aktif.
