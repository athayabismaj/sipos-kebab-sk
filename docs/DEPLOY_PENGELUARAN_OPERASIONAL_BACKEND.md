# Deploy Endpoint Pengeluaran Operasional (Server Aktif)

Dokumen ini untuk mengaktifkan endpoint API pengeluaran operasional agar input dari mobile masuk ke laporan admin.

## 1) Pastikan File Sudah Ada di Backend

Wajib ada:

- `app/Http/Controllers/API/OperationalExpenseController.php`
- `routes/api.php` (berisi route `POST` untuk endpoint pengeluaran)

## 2) Perintah Deploy (Linux/Ubuntu)

Jalankan di direktori project backend (`siinv-kebab-sk`) di server aktif:

```bash
cd /path/to/siinv-kebab-sk

git pull

php artisan route:clear
php artisan config:clear
php artisan cache:clear

php artisan route:list | grep -E "operational-expenses|cashflow/operational-expenses|cashflow/expenses|expenses"
```

## 3) Perintah Deploy (Windows Server / PowerShell)

```powershell
cd E:\path\to\siinv-kebab-sk

git pull

php artisan route:clear
php artisan config:clear
php artisan cache:clear

php artisan route:list | findstr /I "operational-expenses cashflow/operational-expenses cashflow/expenses expenses"
```

## 4) Endpoint yang Harus Aktif

Minimal salah satu route berikut harus tampil:

- `POST /api/operational-expenses`
- `POST /api/cashflow/operational-expenses`
- `POST /api/cashflow/expenses`
- `POST /api/expenses`

## 5) Verifikasi Cepat via cURL

Ganti `BASE_URL` dan `TOKEN_KASIR_VALID`:

```bash
curl -X POST "BASE_URL/api/operational-expenses" \
  -H "Authorization: Bearer TOKEN_KASIR_VALID" \
  -H "Content-Type: application/json" \
  -d '{"amount":6000,"source":"kopi","note":"haus"}'
```

Jika sukses, respons:

```json
{
  "success": true,
  "message": "Pengeluaran operasional berhasil disimpan."
}
```

## 6) Jika Masih 404

Artinya server aktif belum memakai kode terbaru atau cache route belum bersih.
Ulangi langkah 2/3, lalu pastikan route benar-benar muncul di `route:list`.
