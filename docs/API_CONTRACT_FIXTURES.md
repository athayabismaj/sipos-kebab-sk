# API Contract Fixtures

Fixture Fase 5C berada di `app/src/test/resources/contracts`. Semua data adalah
fiktif dan stabil; tidak ada token, password, credential, atau data production.

Daftar fixture:

- `login_success.json`
- `login_validation_error.json`
- `profile_success.json`
- `profile_without_branch.json`
- `menu_success.json`
- `payment_methods_success.json`
- `stock_session_open.json`
- `stock_session_closed.json`
- `checkout_request.json`
- `checkout_success.json`
- `checkout_validation_error.json`
- `checkout_stock_conflict.json`
- `transaction_history_page_1.json`
- `transaction_history_empty.json`
- `transaction_detail.json`
- `receipt.json`
- `unauthorized.json`
- `forbidden.json`
- `not_found.json`

## Aturan sinkronisasi

1. Buktikan kontrak pada test Laravel di `tests/Feature/API/Android`.
2. Perbarui fixture hanya dari response fiktif test backend tersebut.
3. Jalankan test parser/repository Android yang memakai fixture.
4. Jangan mengubah fixture untuk menyembunyikan mismatch; periksa backend dan
   DTO terlebih dahulu.
5. Field nullable harus ditulis eksplisit hanya jika serializer backend memang
   mengirimkannya. Gson Android menghilangkan properti request yang null.

Fixture memvalidasi bentuk data, bukan menggantikan pengujian authorization
backend atau end-to-end dengan server nyata.
