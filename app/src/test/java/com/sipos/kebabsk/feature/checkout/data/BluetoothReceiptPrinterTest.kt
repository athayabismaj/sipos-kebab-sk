package com.sipos.kebabsk.feature.checkout.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothReceiptPrinterTest {

    @Test
    fun print_whenConnected_delegatesBytesOnce() = runTest {
        val connection = FakeReceiptPrinterConnection(isConnected = true)
        val printer = BluetoothReceiptPrinter(connection)
        val bytes = byteArrayOf(0x1B, 0x40)

        val result = printer.print(bytes)

        assertTrue(result.isSuccess)
        assertEquals(1, connection.printCalls)
        assertArrayEquals(bytes, connection.lastBytes)
        assertTrue(printer.isConnected)
    }

    @Test
    fun print_whenConnectionFails_returnsFailure() = runTest {
        val connection = FakeReceiptPrinterConnection(
            isConnected = true,
            result = Result.failure(IllegalStateException("printer gagal"))
        )
        val printer = BluetoothReceiptPrinter(connection)

        val result = printer.print(byteArrayOf(1))

        assertTrue(result.isFailure)
        assertEquals("printer gagal", result.exceptionOrNull()?.message)
        assertEquals(1, connection.printCalls)
    }

    @Test(expected = CancellationException::class)
    fun print_whenConnectionCancels_rethrowsCancellation() = runTest {
        val connection = FakeReceiptPrinterConnection(
            isConnected = true,
            cancellation = CancellationException("cancelled")
        )
        val printer = BluetoothReceiptPrinter(connection)

        printer.print(byteArrayOf(1))
    }

    @Test
    fun isConnected_reflectsConnectionState() {
        val connection = FakeReceiptPrinterConnection(isConnected = false)
        val printer = BluetoothReceiptPrinter(connection)

        assertFalse(printer.isConnected)
    }
}

private class FakeReceiptPrinterConnection(
    override val isConnected: Boolean,
    private val result: Result<Unit> = Result.success(Unit),
    private val cancellation: CancellationException? = null
) : ReceiptPrinterConnection {
    var printCalls: Int = 0
    var lastBytes: ByteArray? = null

    override suspend fun print(data: ByteArray): Result<Unit> {
        printCalls += 1
        lastBytes = data
        cancellation?.let { throw it }
        return result
    }
}
