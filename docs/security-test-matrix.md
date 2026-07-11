# Security Test Matrix

Tanggal verifikasi: 4 Juli 2026

Status:

- PASS: sudah diverifikasi lewat command, test otomatis, atau static check yang spesifik.
- PENDING: belum dieksekusi live di staging/device.
- BLOCKED: tidak bisa dianggap selesai untuk production gate.
- N/A: tidak dijalankan karena bukan target Batch 5A.

## Backend Regression

| ID | Komponen | Skenario | Expected Result | Actual Result | Status | Bukti |
|---|---|---|---|---|---|---|
| BE-01 | PHPUnit | Full backend test suite | Semua test lulus | 18 passed, 72 assertions | PASS | `php artisan test` |
| BE-02 | Test env | SQLite memory test | `pdo_sqlite` tersedia | `pdo_sqlite` dan `sqlite3` aktif di PHP CLI | PASS | `php -m`, `php artisan test` |
| BE-03 | Web test asset | Web feature test dapat render halaman | Vite manifest tersedia | `public/build/manifest.json` dibuat oleh `npm run build` | PASS | `npm run build` |
| BE-04 | Route API | List route API dan middleware | Route sensitif memakai `api.token` dan throttle | 21 route API terdaftar; write endpoint guarded | PASS | `php artisan route:list --path=api -v` |
| BE-05 | Blade cache | Template bisa di-cache | Cache sukses | `Blade templates cached successfully` | PASS | `php artisan view:cache` |
| BE-06 | Diff hygiene | Tidak ada whitespace error | Clean | Tidak ada output error | PASS | `git diff --check` |
| BE-07 | Composer audit | Tidak ada advisory aktif | Clean | No security vulnerability advisories found | PASS | `composer audit` |
| BE-08 | NPM audit | Tidak ada advisory moderate+ aktif | Clean | found 0 vulnerabilities | PASS | `npm audit --audit-level=moderate` |
| BE-09 | Void API route | Endpoint void tersedia dan guarded | Route void muncul dengan `api.token`/throttle | `POST api/transactions/{transaction}/void` muncul | PASS | `php artisan route:list --path=api -v` |
| BE-10 | Checkout integrity | Client tidak bisa memalsukan harga | Harga database dipakai | Fake price diabaikan, total sesuai DB | PASS | `ApiTransactionSecurityTest` |
| BE-11 | Void ownership | Kasir tidak bisa void transaksi kasir lain | 403 dan transaksi tetap sukses | Test lulus | PASS | `ApiTransactionSecurityTest` |
| BE-12 | Void audit | Void sah mencatat audit trail | `status`, `voided_by`, `voided_at`, `void_reason` terisi | Test lulus | PASS | `ApiTransactionSecurityTest` |
| BE-13 | Change password token lifecycle | Token device lain dicabut setelah password berubah | Token lain mendapat 401, token request saat ini tetap valid | Test lulus | PASS | `ApiAuthSecurityTest` |

## Mobile Android Regression

| ID | Komponen | Skenario | Expected Result | Actual Result | Status | Bukti |
|---|---|---|---|---|---|---|
| MOB-01 | Build | Compile Kotlin debug | Build sukses | BUILD SUCCESSFUL | PASS | `./gradlew :app:compileDebugKotlin` |
| MOB-02 | Diff hygiene | Whitespace check | Tidak ada whitespace error | Hanya warning line ending, tidak ada error | PASS | `git diff --check` |
| MOB-03 | Token storage | Token tidak plaintext di SharedPreferences | Token terenkripsi Keystore | Static check: AES-GCM AndroidKeyStore dipakai | PASS | `AppSessionStore.kt` |
| MOB-04 | 401 handling | Token invalid menghapus session | Local session clear + force logout | Static check: interceptor 401 clear session | PASS | `NetworkModule.kt` |
| MOB-05 | 403 handling | Akses ditolak tidak logout paksa | Pesan akses ditolak | Static check: mapper membedakan 401 dan 403 | PASS | `UserFacingError.kt` |
| MOB-06 | Release logging | Release tidak log token/password/OTP | Logging release off | Static check: logging hanya `BuildConfig.DEBUG` | PASS | `NetworkModule.kt` |
| MOB-07 | Manual device | Logout menghapus session file | File session kosong/hilang | Belum diuji di device | PENDING | Manual test |
| MOB-08 | Manual release | Logcat release tidak membocorkan data sensitif | Tidak ada token/password/OTP | Belum diuji APK release device | PENDING | Manual test |

## Backend-Mobile Integration Matrix

| ID | Skenario | Expected Result | Actual Result | Status | Catatan |
|---|---|---|---|---|---|
| INT-01 | Login kasir dari mobile | Login sukses dan role kasir diterima | Belum dieksekusi live | PENDING | Butuh staging backend aktif dan akun uji |
| INT-02 | Checkout transaksi normal | Transaksi tersimpan, total sesuai harga database | Dicakup backend API test; live mobile pending | PENDING | Perlu device/emulator |
| INT-03 | Checkout dengan harga palsu | Harga client diabaikan | PASS di backend API test | PASS | `ApiTransactionSecurityTest` |
| INT-04 | Role non-kasir write endpoint | 403 akses tidak diizinkan | Belum dieksekusi live | PENDING | Butuh token role non-kasir |
| INT-05 | Expense mobile | Expense tersimpan dengan tanggal server | Belum dieksekusi live | PENDING | Butuh staging data |
| INT-06 | Close daily stock session | Sesi tertutup dan mobile keluar flow | Belum dieksekusi live | PENDING | Butuh sesi harian open |
| INT-07 | Void transaksi sah | Void sukses, audit trail tercatat | PASS di backend API test | PASS | Live mobile pending |
| INT-08 | Void transaksi kasir lain | 403 dan transaksi tidak berubah | PASS di backend API test | PASS | Live mobile pending |
| INT-09 | Void transaksi VOID ulang | Ditolak kecuali retry idempotent yang sama | Belum ada test eksplisit | PENDING | Tambahkan test sebelum release final jika flow void dipakai aktif |
| INT-10 | Change password mencabut token device lain | Request device lain berikutnya 401 | PASS di backend API test | PASS | Live mobile pending |
| INT-11 | Reset password mencabut token lama | Request mobile berikutnya 401 dan logout lokal | Static mobile pass; live pending | PENDING | Butuh staging |
| INT-12 | 403 mobile UX | Pesan akses ditolak tanpa logout paksa | Static pass; live pending | PENDING | Butuh role non-kasir |
| INT-13 | 422 mobile UX | Pesan validasi aman | Static sebagian; live pending | PENDING | Butuh skenario validasi |
| INT-14 | 500 mobile UX | Pesan generik | Static sebagian; live pending | PENDING | Simulasi staging aman |

## Manual Mobile Checklist

- Login normal berhasil.
- Logout menghapus session lokal.
- Token lama setelah change password menerima 401 dan user logout lokal.
- Token lama setelah reset password menerima 401 dan user logout lokal.
- 403 menampilkan `Akses tidak diizinkan.` tanpa logout paksa.
- OTP tidak tersimpan setelah reset.
- Token tidak terlihat plaintext di file preferences.
- Logcat release tidak mencetak Authorization/token/password/OTP.
- Debug/local tetap bisa memakai URL development.
- Release memakai HTTPS production.
