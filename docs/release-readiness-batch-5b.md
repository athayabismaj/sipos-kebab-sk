# Release Readiness Batch 5B

Tanggal verifikasi: 4 Juli 2026

## Ringkasan

Batch 5B menjalankan regression verification backend/mobile dan mencoba menyiapkan live mobile-backend integration test. Hasilnya: kode backend dan mobile stabil untuk staging, tetapi belum layak dinyatakan production-ready karena live device/emulator integration test tidak bisa dijalankan dari environment ini.

Keputusan: Ready for Staging Only.

## Staging/Local Environment

| Item | Actual | Status | Catatan |
|---|---|---|---|
| Backend reachable local | `http://127.0.0.1:8000/api/menus` mengembalikan 401 | PASS | Endpoint hidup dan guarded |
| Mobile debug URL | `http://192.168.1.5:8000/api/` | FAIL | Tidak reachable dari host saat dicek |
| Laravel environment | `local` | FAIL untuk staging-like | Belum staging |
| Laravel debug | `true` | FAIL untuk staging-like | Simulasi production harus `APP_DEBUG=false` |
| DB host | `127.0.0.1` | PASS untuk local-only | Tidak menunjukkan production |
| DB name | `laravel` | PASS untuk local-only | Jangan dipakai sebagai production evidence |
| Android SDK adb | Ada di SDK default | PASS | `adb.exe` ditemukan |
| Device/emulator | Tidak ada device di `adb devices` | BLOCKED | Device verification belum bisa dijalankan |

## Backend Verification

| Command | Result | Status |
|---|---|---|
| `php artisan test` | 18 passed, 72 assertions | PASS |
| `composer audit` | No security vulnerability advisories found | PASS |
| `npm audit --audit-level=moderate` | found 0 vulnerabilities | PASS |
| `php artisan route:list --path=api -v` | 21 API routes, including void route | PASS |
| `php artisan route:list --path=developer -v` | No matching routes | PASS |
| `php artisan view:cache` | Blade templates cached successfully | PASS |
| `npm run build` | Vite build succeeded | PASS |
| `git diff --check` backend | No output | PASS |

## Mobile Verification

| Command/Check | Result | Status |
|---|---|---|
| `./gradlew :app:compileDebugKotlin` | BUILD SUCCESSFUL | PASS |
| `./gradlew :app:assembleDebug` | BUILD SUCCESSFUL | PASS |
| `./gradlew :app:assembleRelease` | BUILD SUCCESSFUL | PASS |
| `git diff --check` mobile | Only line-ending warnings | PASS |
| Debug manifest `allowBackup` | `false` | PASS |
| Debug manifest `usesCleartextTraffic` | `true` | PASS for local debug |
| Debug manifest `debuggable` | `true` | PASS for debug |
| Release manifest `allowBackup` | `false` | PASS |
| Release manifest `usesCleartextTraffic` | `false` | PASS |
| Release manifest `debuggable` | Not present | PASS |

## Integration Test Matrix

| ID | Modul | Langkah uji | Expected result | Actual result | Status | Bukti/Catatan |
|---|---|---|---|---|---|---|
| INT-A1 | Auth | Login kasir dari mobile | Login sukses | Tidak dijalankan | BLOCKED | Tidak ada device/emulator |
| INT-A2 | Auth | Logout mobile | Session lokal terhapus | Tidak dijalankan | BLOCKED | Tidak ada device/emulator |
| INT-A3 | Auth | Token invalid/expired | 401 dan mobile force logout | Static mobile pass; live tidak dijalankan | PENDING | Perlu device |
| INT-A4 | Auth | Role tidak sah akses write | 403 tanpa logout paksa | Static mobile pass; live tidak dijalankan | PENDING | Perlu token role non-kasir live |
| INT-B1 | Checkout | Checkout normal | Transaksi berhasil | Dicakup backend test sebagian | PENDING | Live mobile belum |
| INT-B2 | Checkout | Harga palsu client | Backend pakai harga database | Backend API test pass | PASS | `ApiTransactionSecurityTest` |
| INT-B3 | Checkout | Response tidak bocor data sensitif | Tidak ada secret/stack trace | Tidak dijalankan live | PENDING | Perlu capture response staging |
| INT-C1 | Expense | Create expense normal | Expense tersimpan | Tidak dijalankan live | PENDING | Perlu akun/data staging |
| INT-C2 | Expense | Client kirim `entry_date` | Backend pakai tanggal server | Tidak dijalankan live | PENDING | Perlu test/API smoke khusus |
| INT-D1 | Daily stock | Lihat sesi stok | Data tampil | Tidak dijalankan live | PENDING | Perlu device/data staging |
| INT-D2 | Daily stock | Close session | Sesi tertutup | Tidak dijalankan live | PENDING | Perlu sesi open |
| INT-E1 | Void | Void transaksi sendiri | Success dan audit terisi | Backend API test pass | PASS | `ApiTransactionSecurityTest` |
| INT-E2 | Void | Void transaksi kasir lain | 403 dan tidak berubah | Backend API test pass | PASS | `ApiTransactionSecurityTest` |
| INT-E3 | Void | Void ulang transaksi VOID | Ditolak kecuali retry idempotent sama | Belum ada test eksplisit | PENDING | Tambahkan sebelum production |
| INT-E4 | Void | Session salah | Ditolak | Tidak dijalankan live | PENDING | Perlu test eksplisit |
| INT-F1 | Password | Reset password | Token lama 401 | Static/backend code supports token revoke | PENDING | Live OTP/email belum |
| INT-F2 | Password | Change password | Token device lain 401 | Backend API test pass | PASS | `ApiAuthSecurityTest` |
| INT-G1 | Error handling | 422 validation | Pesan aman | Tidak dijalankan live | PENDING | Perlu staging scenarios |
| INT-G2 | Error handling | 500 | Pesan generik tanpa stack trace | Tidak dijalankan live | PENDING | Perlu simulasi aman |

## Bug Baru Ditemukan dan Ditutup

| ID | Severity | Temuan | Perbaikan | Status |
|---|---|---|---|---|
| B5B-01 | High | API `changePassword()` belum mencabut token device lain | Token lain user dihapus setelah password berubah; token request saat ini tetap valid | Closed |

## Git Hygiene

File IDE/temporary:

- `.idea/deploymentTargetSelector.xml`: tidak dirty saat final check sebelumnya, tidak boleh commit.
- `.env`: tidak boleh commit.
- `local.properties`: tidak boleh commit.
- `storage/logs/*`, database dump, backup, keystore: tidak boleh commit.
- `app/build/*` Android: build artifact, tidak boleh commit.

Backend file berubah/tracked:

- `.env.example`
- `app/Http/Controllers/API/AuthController.php`
- `app/Http/Controllers/API/MenuController.php`
- `app/Http/Controllers/API/TransactionController.php`
- `app/Models/Transaction.php`
- `app/Services/ApiTransactionService.php`
- `composer.lock`
- `package-lock.json`
- `resources/views/partials/sidebar_admin.blade.php`
- `routes/api.php`
- `routes/web.php`
- `tests/Feature/Owner/OwnerCriticalFlowTest.php`

Backend file baru:

- `app/Http/Controllers/API/DailyStockController.php`
- `app/Http/Controllers/API/OperationalExpenseController.php`
- `app/Http/Controllers/Admin/DailyStockSessionController.php`
- `app/Models/DailyStockSession.php`
- `app/Services/DailySessionService.php`
- `database/migrations/2026_04_26_010000_create_daily_stock_sessions_table.php`
- `database/migrations/2026_07_04_000001_add_void_audit_fields_to_transactions_table.php`
- `resources/views/admin/daily_stock_sessions/`
- `tests/Feature/Api/`

Mobile file berubah/tracked:

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/sipos/kebabsk/common/AppSessionStore.kt`
- `app/src/main/java/com/sipos/kebabsk/common/UserFacingError.kt`
- `app/src/main/java/com/sipos/kebabsk/data/network/NetworkModule.kt`
- `app/src/main/java/com/sipos/kebabsk/feature/auth/data/repository/AuthRepositoryImpl.kt`
- `app/src/main/java/com/sipos/kebabsk/feature/auth/presentation/forgotpassword/ForgotPasswordScreen.kt`
- `app/src/main/java/com/sipos/kebabsk/feature/auth/presentation/forgotpassword/ForgotPasswordViewModel.kt`
- `app/src/main/java/com/sipos/kebabsk/feature/auth/presentation/login/LoginScreen.kt`
- `app/src/main/java/com/sipos/kebabsk/feature/auth/presentation/login/LoginViewModel.kt`
- `app/src/main/java/com/sipos/kebabsk/feature/transactions/presentation/TransactionsViewModel.kt`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`

Mobile docs baru:

- `docs/security-hardening-summary.md`
- `docs/security-test-matrix.md`
- `docs/residual-risk-register.md`
- `docs/security-deployment-checklist.md`
- `docs/release-readiness-batch-5b.md`

## Rencana Commit

Backend:

1. `security: harden api transaction authorization and pricing`
2. `security: revoke stale api tokens after password changes`
3. `test: add api security regression tests`
4. `chore: update dependencies after security audit`
5. `docs: add release readiness evidence`

Mobile:

1. `security: encrypt session storage and handle unauthorized session`
2. `security: harden okhttp logging and network config`
3. `security: disable backup and protect sensitive local data`
4. `docs: add mobile security test documentation`

## Residual Risk Final

- Live Android device/emulator integration test belum berjalan.
- Backend local masih `APP_DEBUG=true`, belum staging-like.
- Mobile debug URL saat ini tidak reachable dari host.
- Void idempotency retry dan wrong-session scenario perlu test eksplisit tambahan.
- Secure storage/logcat release perlu inspeksi device.
- Production `.env` HTTPS/CORS/session secure belum diverifikasi dari sesi ini.

## Rekomendasi Sebelum Production

1. Jalankan backend staging dengan `APP_DEBUG=false`, database staging, dan URL yang reachable dari device.
2. Hubungkan emulator/device, lalu jalankan matrix INT-A sampai INT-G dari aplikasi Android.
3. Tambahkan test eksplisit untuk void ulang dan wrong-session.
4. Verifikasi logcat release dan file preferences device.
5. Pisahkan commit sesuai rencana dan jangan commit `.env`, `local.properties`, `.idea`, log, dump, keystore, atau build artifact.
