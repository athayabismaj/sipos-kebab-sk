# Android Contract Testing

## Coverage

Test Fase 5C mencakup:

- parsing login, role, branch, dan branch nullable;
- persistence dan pembersihan session;
- logout backend pada sukses, 401, timeout, dan pemanggilan ganda;
- checkout request/response dan nominal `Long`;
- guard double-submit, loading, success, failure, dan cancellation;
- paginator transaksi, status sukses/void, empty page, dan detail receipt;
- formatter receipt dari fixture, nama panjang, nominal besar, dan non-ASCII;
- sesi stok terbuka, belum terbuka, serta payload penutupan endpoint aktif;
- error mapping 401, 403, 404, 409, 422, 429, 500, 503, timeout, dan jaringan.

## Menjalankan

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

Test terfokus dapat memakai `--tests "*CheckoutContractFixtureTest"` dan pola
kelas lain. Gunakan Gradle wrapper repository; tidak perlu dependency baru.

## Logout

Urutan aman adalah membaca token, memanggil `POST /api/auth/logout`, kemudian
membersihkan session lokal pada blok `finally`. Session tetap dibersihkan bila
token sudah 401 atau jaringan gagal agar device tidak tertahan dalam state login.

## Branch

Branch berasal dari response backend dan disimpan sebagai ID, nama, dan kode
nullable di `AuthSession`. Client tidak memakai branch sebagai pengganti policy
backend dan tidak mengirim `branch_id` pada checkout/tutup sesi.

## Endpoint stok

Endpoint aktif adalah `POST /daily-stock-sessions/close`. Deklarasi lama
`POST /sessions/{id}/close` dan rantai kode matinya telah dihapus setelah audit
menunjukkan tidak ada pemanggil UI/navigation aktif.

## Batasan

Unit test tidak membuktikan emulator/device, production base URL, process death,
jaringan nyata, printer fisik, signing, atau release APK. Checkout tidak retry
otomatis; response timeout setelah server commit tetap merupakan risiko ambigu.
