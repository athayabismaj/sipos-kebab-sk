# Security Hardening Summary

Tanggal verifikasi: 4 Juli 2026

Dokumen ini merangkum hasil hardening backend Laravel `siinv-kebab-sk` dan mobile Android `sipos-kebab-sk` sampai Batch 5A. Fokus Batch 5A adalah menutup blocker release readiness tanpa refactor besar, tanpa menyentuh production, dan tanpa upgrade dependency mayor.

## Ringkasan Keputusan Batch 5A

Status akhir: belum direkomendasikan production release penuh sampai live staging integration test selesai. Blocker teknis utama sudah ditutup, tetapi bukti E2E mobile-backend di staging belum lengkap.

- API void transaction dipulihkan, bukan flow mobile dinonaktifkan.
- Route final: `POST /api/transactions/{transaction}/void`.
- Middleware final: `api`, `api.token`, `throttle:api-checkout-role-aware`.
- PHPUnit backend final: PASS, 18 tests, 72 assertions.
- `composer audit` final: PASS, tidak ada advisory.
- `npm audit --audit-level=moderate` final: PASS, 0 vulnerability.
- `git diff --check` backend final: PASS.
- Mobile compile final: PASS untuk `./gradlew :app:compileDebugKotlin`.

## Perbaikan Backend Batch 5A

- Mengaktifkan PHP CLI extension `pdo_sqlite` dan `sqlite3` secara permanen di `C:\php\php.ini`.
- Menjalankan `npm install` dan `npm run build` agar `public/build/manifest.json` tersedia untuk web feature test.
- Menambahkan route API void transaction yang guarded dan rate-limited.
- Menambahkan audit field transaksi: `status`, `voided_by`, `voided_at`, `void_reason`, `void_idempotency_key`.
- Menambahkan validasi ownership/session/date/idempotency untuk void transaksi.
- Menolak void transaksi milik kasir lain.
- Mengabaikan harga dari client saat checkout dan selalu memakai harga database.
- Mencabut token device lain setelah API change password.
- Memperbaiki test owner period agar deterministik dengan `Carbon::setTestNow`.
- Membersihkan blank line EOF pada backend.
- Menghapus nilai API key nyata dari `.env.example` dan menggantinya dengan placeholder kosong.

## Dependency Remediation

Composer update terkontrol dilakukan tanpa upgrade mayor.

| Package | Sebelum | Sesudah | Catatan |
|---|---:|---:|---|
| `laravel/framework` | 12.51.0 | 12.62.0 | Patch/minor Laravel 12, bukan Laravel 13 |
| `guzzlehttp/guzzle` | 7.10.0 | 7.13.1 | Patch/minor |
| `guzzlehttp/psr7` | 2.8.0 | 2.12.3 | Patch/minor |
| `league/commonmark` | 2.8.0 | 2.8.2 | Patch/minor |
| `laravel/breeze` | 2.3.8 | 2.4.2 | Patch/minor |
| `laravel/pail` | 1.2.6 | 1.2.7 | Patch |
| `laravel/pint` | 1.27.1 | 1.29.3 | Patch/minor |
| `laravel/sail` | 1.53.0 | 1.63.0 | Patch/minor |
| `nunomaduro/collision` | 8.9.0 | 8.9.4 | Patch |
| `resend/resend-php` | 1.2.0 | 1.4.0 | Minor |

Major update yang sengaja tidak dilakukan: Laravel 13, Tinker 3, PHPUnit 12.

NPM dependency juga diperbaiki dengan `npm audit fix` non-force. Hasil akhir `npm audit --audit-level=moderate`: 0 vulnerability.

## Verifikasi Final

- `php artisan test`: PASS, 18 tests, 72 assertions.
- `php artisan route:list --path=api -v`: PASS, 21 route API termasuk route void.
- `php artisan view:cache`: PASS.
- `composer audit`: PASS, no security vulnerability advisories found.
- `npm audit --audit-level=moderate`: PASS, found 0 vulnerabilities.
- `npm run build`: PASS, Vite manifest berhasil dibuat.
- Backend `git diff --check`: PASS.
- Android `./gradlew :app:compileDebugKotlin`: PASS.
- Android `git diff --check`: PASS dengan warning line ending saja, tanpa whitespace error.

## Batasan Yang Masih Berlaku

- Live integration test mobile-backend belum dijalankan di staging dengan device/emulator dan akun uji lengkap.
- Void transaction saat ini mencatat audit dan menonaktifkan transaksi dari ringkasan non-void, tetapi belum melakukan restock bahan otomatis karena detail transaksi menyimpan `menu_id`, bukan `variant_id`, sehingga pemetaan bahan tidak cukup aman untuk dikembalikan otomatis.
- `.idea/deploymentTargetSelector.xml` masih dirty dari Android Studio dan tidak boleh ikut commit.
- Backend repo masih memiliki banyak perubahan fitur lama yang bukan seluruhnya berasal dari Batch 5A; staging/release commit harus dipisah dan direview.

## Kesimpulan

Batch 5A menutup blocker teknis utama: test environment backend, Composer/NPM advisories, route void API, whitespace hygiene, dan mobile compile sanity. Keputusan release tetap `NOT READY FOR PRODUCTION` sampai live staging integration test selesai dan file non-commit dipastikan tidak masuk commit.
