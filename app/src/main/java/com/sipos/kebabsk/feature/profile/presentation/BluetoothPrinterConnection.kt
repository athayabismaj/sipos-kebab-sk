package com.sipos.kebabsk.feature.profile.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

/**
 * Singleton yang mengelola koneksi Bluetooth SPP (Serial Port Profile) ke printer thermal.
 *
 * Koneksi dilakukan melalui RFCOMM socket menggunakan UUID SPP standar.
 * Object ini menyimpan referensi socket aktif dan output stream-nya,
 * sehingga bisa digunakan saat mencetak struk dari mana saja di aplikasi.
 */
object BluetoothPrinterConnection {

    /** UUID standar untuk Serial Port Profile (SPP) — digunakan oleh printer thermal ESC/POS */
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var activeSocket: BluetoothSocket? = null
    private var activeOutputStream: OutputStream? = null

    /** Cek apakah socket saat ini masih terhubung */
    val isConnected: Boolean
        get() = activeSocket?.isConnected == true

    /** Alamat MAC perangkat yang sedang terkoneksi, atau null jika tidak ada */
    val connectedAddress: String?
        @SuppressLint("MissingPermission")
        get() = if (isConnected) activeSocket?.remoteDevice?.address else null

    /** Output stream untuk mengirim data print (ESC/POS bytes) */
    val outputStream: OutputStream?
        get() = if (isConnected) activeOutputStream else null

    /**
     * Mencoba membuat koneksi RFCOMM ke perangkat Bluetooth.
     *
     * Fungsi ini HARUS dipanggil dari coroutine (suspend function).
     * Koneksi dilakukan di Dispatchers.IO (background thread).
     *
     * @param device BluetoothDevice yang akan disambungkan
     * @return Result.success jika socket.connect() berhasil,
     *         Result.failure(IOException) jika koneksi ditolak/gagal
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice): Result<Unit> = withContext(Dispatchers.IO) {
        // Tutup koneksi lama jika ada
        disconnect()

        try {
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect() // <-- Ini koneksi NYATA ke hardware
            activeSocket = socket
            activeOutputStream = socket.outputStream
            Result.success(Unit)
        } catch (e: IOException) {
            // Koneksi gagal — perangkat menolak (bukan printer, mati, dll)
            runCatching { activeSocket?.close() }
            activeSocket = null
            activeOutputStream = null
            Result.failure(e)
        }
    }

    /**
     * Menutup koneksi aktif dan membersihkan referensi socket.
     */
    fun disconnect() {
        runCatching { activeOutputStream?.close() }
        runCatching { activeSocket?.close() }
        activeSocket = null
        activeOutputStream = null
    }
}
