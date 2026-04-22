# UAT End-to-End Mobile Kasir (SIPOS Kebab SK)

Dokumen ini dipakai untuk uji penerimaan pengguna (UAT) alur kasir secara end-to-end.

## Tujuan UAT
- Memastikan alur utama kasir berjalan dari login sampai transaksi tersimpan.
- Memastikan skenario gagal menampilkan pesan yang jelas dan tidak teknis.
- Memastikan data sinkron terhadap sesi harian dan timezone server `Asia/Jakarta`.

## Lingkungan Uji
- Aplikasi: Android `debug` terbaru.
- Backend: environment uji/staging yang aktif.
- Role uji:
  - `Admin` untuk buka sesi dan transfer bahan.
  - `Kasir` untuk transaksi.
- Waktu server: `Asia/Jakarta`.

## Data Uji Minimum
- Minimal 2 menu dengan varian aktif.
- Minimal 1 metode pembayaran aktif.
- Minimal 1 akun kasir aktif.
- Stok bahan harian cukup untuk 3 transaksi sukses.

## Skenario UAT Wajib

### TC-01 Login Kasir Berhasil
Langkah:
1. Buka aplikasi.
2. Login dengan akun kasir valid.

Ekspektasi:
1. Masuk ke dashboard kasir.
2. Nama kasir tampil benar.
3. Tidak ada pesan error teknis.

### TC-02 Gate Sesi Harian (Belum Open)
Prasyarat:
1. Admin belum membuka sesi harian.

Langkah:
1. Login sebagai kasir.
2. Masuk dashboard/menu pembayaran.

Ekspektasi:
1. Status sesi menunjukkan belum dibuka.
2. Tombol checkout tidak dapat diproses.
3. Pesan tampil jelas: sesi harian belum dibuka admin.

### TC-03 Gate Sesi Harian (Open)
Prasyarat:
1. Admin sudah membuka sesi harian.

Langkah:
1. Login kasir.
2. Tekan `Mulai Transaksi`.

Ekspektasi:
1. Kasir bisa masuk ke layar transaksi.
2. Status sesi tampil aktif.

### TC-04 Checkout Sukses + Popup Kembalian
Prasyarat:
1. Sesi harian open.
2. Metode pembayaran tersedia.
3. Stok harian bahan cukup.

Langkah:
1. Pilih menu/varian.
2. Tambahkan item ke keranjang.
3. Pilih metode bayar.
4. Input nominal bayar cukup.
5. Tekan `Selesaikan Pembayaran`.

Ekspektasi:
1. Transaksi sukses tersimpan.
2. Muncul popup mini struk berisi:
   - kode transaksi
   - total
   - dibayar
   - kembalian
   - item transaksi
3. Tombol `Bagikan` berfungsi membuka share intent.

### TC-05 Error: Bahan Belum Dibawa ke Stok Harian
Prasyarat:
1. Admin open sesi, tetapi bahan belum ditransfer ke stok harian.

Langkah:
1. Kasir lakukan checkout.

Ekspektasi:
1. Checkout gagal.
2. Muncul pesan bisnis yang mudah dipahami (tanpa istilah teknis).

### TC-06 Error: Stok Harian Tidak Cukup
Prasyarat:
1. Stok harian bahan dibuat kurang.

Langkah:
1. Kasir checkout transaksi yang melebihi stok.

Ekspektasi:
1. Checkout gagal.
2. Pesan menyatakan stok harian tidak cukup.

### TC-07 Error: Pembayaran Kurang
Langkah:
1. Isi nominal bayar di bawah total.
2. Lanjut checkout.

Ekspektasi:
1. Checkout gagal.
2. Pesan menyatakan nominal pembayaran kurang.

### TC-08 Riwayat Transaksi Harian
Langkah:
1. Buka tab `Transaksi`.
2. Pilih filter tanggal hari ini.

Ekspektasi:
1. Transaksi sukses dari TC-04 muncul di riwayat.
2. Detail total/item sesuai.
3. Data sesuai tanggal `Asia/Jakarta`.

### TC-09 Ringkasan Shift Kasir
Langkah:
1. Buka dashboard kasir.

Ekspektasi:
1. Tampil:
   - total transaksi hari ini
   - total item terjual
   - omset hari ini
2. Jika gagal load, tampil pesan user-friendly + tombol `Coba Lagi`.

### TC-10 Keamanan Sesi
Langkah:
1. Login kasir.
2. Trigger token invalid/expired dari backend.

Ekspektasi:
1. Aplikasi auto logout saat menerima `401`.
2. Kembali ke layar login.
3. Sesi lokal terhapus.

### TC-11 Anti Double Submit Checkout
Langkah:
1. Saat proses checkout loading, tekan tombol checkout berulang.

Ekspektasi:
1. Hanya satu request transaksi yang diproses.
2. Tidak ada duplikasi transaksi.

## Format Evidensi UAT (Isi Saat Pengujian)

Gunakan tabel berikut saat eksekusi:

| ID | Tanggal Uji (WIB) | Penguji | Hasil (PASS/FAIL) | Catatan | Bukti (screenshot/video) |
|---|---|---|---|---|---|
| TC-01 |  |  |  |  |  |
| TC-02 |  |  |  |  |  |
| TC-03 |  |  |  |  |  |
| TC-04 |  |  |  |  |  |
| TC-05 |  |  |  |  |  |
| TC-06 |  |  |  |  |  |
| TC-07 |  |  |  |  |  |
| TC-08 |  |  |  |  |  |
| TC-09 |  |  |  |  |  |
| TC-10 |  |  |  |  |  |
| TC-11 |  |  |  |  |  |

## Kriteria Lulus UAT
- Semua test case wajib `PASS`.
- Tidak ada pesan teknis ditampilkan ke kasir.
- Tidak ada transaksi ganda pada uji anti double-submit.
- Data transaksi, riwayat, dan ringkasan shift konsisten.
