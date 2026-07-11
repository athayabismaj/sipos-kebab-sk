# Residual Risk Register

Tanggal verifikasi: 4 Juli 2026

| ID | Risiko | Severity | Status | Alasan | Mitigasi Sementara | Rekomendasi Lanjutan |
|---|---|---|---|---|---|---|
| RR-01 | Backend PHPUnit belum lulus | High | Closed | `php artisan test` final PASS, 18 tests, 72 assertions | Tidak perlu mitigasi | Jalankan rutin di CI |
| RR-02 | Composer advisories aktif | High | Closed | `composer audit` final clean | Tidak perlu mitigasi | Aktifkan dependency audit di CI |
| RR-03 | NPM advisories aktif | High | Closed | `npm audit fix` non-force berhasil; audit final 0 vulnerability | Tidak perlu mitigasi | Aktifkan `npm audit --audit-level=moderate` di CI |
| RR-04 | Endpoint API void transaction tidak muncul | High | Closed | Route `POST /api/transactions/{transaction}/void` sudah terdaftar dan diuji | Tidak perlu mitigasi | Tambahkan test idempotent retry |
| RR-05 | Live staging integration test belum selesai | High | Release Blocker | Tidak ada device/emulator terhubung di `adb devices`, dan mobile debug URL tidak reachable dari host | Jangan production release final | Jalankan matriks INT-01 sampai INT-14 di staging/device |
| RR-06 | Void belum restock bahan otomatis | Medium | Accepted Risk | `transaction_details` menyimpan `menu_id`, bukan `variant_id`; restock otomatis berisiko salah bahan/varian | Void mencatat audit dan mengecualikan transaksi dari ringkasan non-void | Tambahkan struktur detail varian/resep snapshot sebelum restock otomatis |
| RR-07 | Certificate pinning belum diterapkan | Medium | Accepted Risk | Pinning butuh proses rotasi pin yang matang; salah konfigurasi bisa memutus operasional | Wajib HTTPS valid, release cleartext off | Implement pinning hanya jika threat model MITM tinggi dan rotasi sertifikat siap |
| RR-08 | Secure storage belum diuji instrumented di device | Medium | Pending External Verification | Static/build check pass, tetapi belum inspect file preferences di emulator/device | Manual checklist tersedia | Tambahkan instrumented test secure storage |
| RR-09 | API `Cache-Control: no-store` belum eksplisit | Medium | Future Improvement | Static audit belum menemukan middleware no-store untuk endpoint sensitif | Android disk cache API sudah dihapus | Tambahkan middleware no-store untuk auth/profile/transaction/report/stock/expense |
| RR-10 | PostgreSQL staging backup/restore belum diuji | Medium | Pending External Verification | `pg_dump` dan `pg_restore` tidak tersedia di PATH lokal | Jangan restore production | Siapkan staging PostgreSQL asli dengan data dummy |
| RR-11 | `.idea/deploymentTargetSelector.xml` dirty | Low | Must Not Commit | File IDE lokal berubah otomatis oleh Android Studio | Jangan stage/commit file ini | Revert lokal atau tambahkan `.idea/deploymentTargetSelector.xml` ke `.gitignore` sesuai kebijakan tim |
| RR-12 | Backend repo masih dirty dari perubahan lama | Medium | Needs Owner Review | Banyak file backend berubah/untracked di luar patch Batch 5A | Pisahkan commit dan review per domain | Commit terpisah: security blocker, dependency, docs/mobile |

## Dependency Notes

Composer advisory awal sudah tidak dapat direproduksi setelah update karena `composer audit` final bersih. Paket yang diperbarui mencakup Laravel 12 patch/minor, Guzzle, PSR-7, CommonMark, Symfony transitive packages, Breeze, Pail, Pint, Sail, Collision, dan Resend. Upgrade mayor sengaja tidak dilakukan.

NPM advisory awal mencakup `axios`, `vite`, `rollup`, `esbuild`, `form-data`, `follow-redirects`, `picomatch`, `postcss`, `shell-quote`, dan `yaml`. `npm audit fix` non-force menutup semuanya dan audit final menunjukkan 0 vulnerability.

## Certificate Pinning Decision

Status saat ini: not implemented.

Keputusan: accepted risk untuk kondisi operasional kecil/internal dengan HTTPS valid. Pinning menjadi required jika aplikasi digunakan di jaringan tidak tepercaya dengan risiko MITM tinggi, perangkat banyak tersebar, dan tim sudah memiliki proses rotasi certificate pin. Tanpa proses rotasi, pinning berisiko membuat aplikasi tidak bisa konek saat sertifikat production berubah.

## Secure Storage Instrumented Test Checklist

- Login dan pastikan session lama plaintext termigrasi ke encrypted key.
- Verifikasi file `sipos_session.xml` tidak berisi token plaintext.
- Logout dan pastikan file session kosong atau tidak memuat token.
- Trigger 401 dari backend dan pastikan session lokal terhapus.
- Clear app data dan pastikan session hilang.
