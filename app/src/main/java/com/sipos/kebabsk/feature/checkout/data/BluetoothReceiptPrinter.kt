package com.sipos.kebabsk.feature.checkout.data

import com.sipos.kebabsk.feature.checkout.domain.ReceiptPrinter
import com.sipos.kebabsk.feature.profile.presentation.BluetoothPrinterConnection

interface ReceiptPrinterConnection {
    val isConnected: Boolean
    suspend fun print(data: ByteArray): Result<Unit>
}

object AndroidReceiptPrinterConnection : ReceiptPrinterConnection {
    override val isConnected: Boolean
        get() = BluetoothPrinterConnection.isConnected

    override suspend fun print(data: ByteArray): Result<Unit> {
        return BluetoothPrinterConnection.print(data)
    }
}

class BluetoothReceiptPrinter(
    private val connection: ReceiptPrinterConnection = AndroidReceiptPrinterConnection
) : ReceiptPrinter {
    val isConnected: Boolean
        get() = connection.isConnected

    override suspend fun print(data: ByteArray): Result<Unit> {
        return connection.print(data)
    }
}
