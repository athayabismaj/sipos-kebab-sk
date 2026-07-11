# Security Deployment Checklist

Tanggal verifikasi: 4 Juli 2026

Checklist ini dipakai sebelum production release backend Laravel `siinv-kebab-sk` dan Android `sipos-kebab-sk`.

## Release Decision

Status saat ini: NOT READY FOR PRODUCTION FINAL.

Alasan:

- Blocker teknis Batch 5A sudah ditutup: PHPUnit pass, Composer/NPM audit clean, route void tersedia, diff hygiene backend clean.
- Live integration test mobile-backend di staging belum dijalankan penuh.
- `.idea/deploymentTargetSelector.xml` masih dirty dan tidak boleh ikut commit.
- Backend repo masih berisi banyak perubahan lama/untracked yang perlu dipisahkan sebelum commit/release.

## Backend Final Checklist

| Item | Status | Bukti/Catatan |
|---|---|---|
| Full PHPUnit lulus | PASS | `php artisan test`: 18 passed, 72 assertions |
| `pdo_sqlite` tersedia untuk test | PASS | PHP CLI extension aktif |
| Vite asset manifest tersedia | PASS | `npm run build` membuat `public/build/manifest.json` |
| API route list diverifikasi | PASS | `php artisan route:list --path=api -v` |
| Void API route tersedia | PASS | `POST api/transactions/{transaction}/void` |
| Void API middleware | PASS | `api`, `api.token`, `throttle:api-checkout-role-aware` |
| Blade view cache berhasil | PASS | `php artisan view:cache` |
| `git diff --check` bersih | PASS | Tidak ada whitespace error |
| Composer audit bersih | PASS | No security vulnerability advisories found |
| NPM audit bersih | PASS | found 0 vulnerabilities |
| Production HTTPS aktif | PENDING | Verifikasi deployment final |
| `APP_FORCE_HTTPS` production aktif | PENDING | Verifikasi `.env` production, jangan commit secret |
| Session cookie secure/same-site production | PENDING | Verifikasi `.env` production |
| CORS production berbasis allowlist | PENDING | Verifikasi `.env` production |
| API no-store cache header | FUTURE | Middleware/header belum eksplisit |
| PostgreSQL staging backup/restore | PENDING | Butuh `pg_dump`/`pg_restore` dan staging DB dummy |

## Android Final Checklist

| Item | Status | Bukti/Catatan |
|---|---|---|
| Compile debug Kotlin | PASS | `./gradlew :app:compileDebugKotlin` |
| Diff hygiene | PASS | Hanya warning line ending, tanpa whitespace error |
| Token encrypted storage | PASS | Android Keystore AES-GCM static check |
| 401 clears local session | PASS | OkHttp interceptor clears session and emits force logout |
| 403 does not force logout | PASS | Error mapper membedakan 401 vs 403 |
| Release HTTP logging off | PASS | Logging interceptor hanya dipasang saat `BuildConfig.DEBUG` |
| Authorization redacted in debug | PASS | `redactHeader("Authorization")` |
| Token plaintext device check | PENDING | Perlu emulator/device inspection |
| Release logcat sensitive check | PENDING | Perlu install release/staging build |

## Backend-Mobile Integration Checklist

| Item | Status | Catatan |
|---|---|---|
| Login kasir mobile | PENDING | Butuh staging backend aktif |
| Checkout normal | PENDING | Butuh data menu/stok/payment method |
| Checkout harga palsu tidak memengaruhi total | PASS | Backend API test pass; live mobile pending |
| Role non-kasir write endpoint ditolak | PENDING | Butuh token role non-kasir |
| Expense mobile memakai tanggal server | PENDING | Butuh staging data |
| Close daily stock session | PENDING | Butuh sesi harian open |
| Void transaksi sah | PASS | Backend API test pass; live mobile pending |
| Void transaksi kasir lain gagal | PASS | Backend API test pass; live mobile pending |
| Void transaksi VOID ulang | PENDING | Tambahkan test idempotency eksplisit |
| Change password mencabut token device lain | PASS | Backend API test pass; live mobile pending |
| Reset password mencabut token lama | PENDING | Backend live/staging diperlukan |
| Mobile 401/403/422/500 message | PENDING | Static check sebagian; live staging belum |

## File Yang Tidak Boleh Di-Commit

- `local.properties`
- `.idea/deploymentTargetSelector.xml`
- `.env`
- File backup database hasil dump
- APK keystore/signing key
- Token API, password, OTP, credential SMTP, credential database

## Commit Separation

Commit yang disarankan:

1. Backend security blocker fix: void route/service/controller/model/migration/test, checkout price trust fix, test determinism, `.env.example` placeholder.
2. Dependency updates: `composer.lock`, `package-lock.json`, built asset jika memang policy repo meng-commit `public/build`.
3. Documentation updates: file `docs/security-*.md`.
4. Mobile hardening cleanup: perubahan Android non-IDE.

Jangan masukkan `.idea/deploymentTargetSelector.xml` ke commit.

## Production Release Gate

Sebelum release production final:

1. Jalankan live staging integration test INT-01 sampai INT-14.
2. Verifikasi production HTTPS, CORS allowlist, secure session cookie, dan `APP_FORCE_HTTPS`.
3. Verifikasi Android release dengan backend staging/production-like HTTPS.
4. Pastikan token tidak plaintext di device dan release logcat tidak membocorkan data sensitif.
5. Putuskan apakah void perlu restock otomatis; jika ya, tambahkan snapshot detail varian/resep terlebih dahulu.
6. Pisahkan commit dan pastikan `.idea/deploymentTargetSelector.xml` tidak ikut.
