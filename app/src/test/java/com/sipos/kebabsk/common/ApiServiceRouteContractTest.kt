package com.sipos.kebabsk.common

import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.dailystock.data.remote.DailyStockApiService
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseApiService
import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.POST

class ApiServiceRouteContractTest {
    @Test
    fun retrofitRoutesMatchCurrentBackendContract() {
        assertRoute<AuthApiService>("login", "POST", "auth/login")
        assertRoute<AuthApiService>("me", "GET", "auth/me")
        assertRoute<AuthApiService>("logout", "POST", "auth/logout")
        assertRoute<AuthApiService>("sessionCurrentStatus", "GET", "sessions/current-status")

        assertRoute<MenuApiService>("getMenus", "GET", "menus")
        assertRoute<CheckoutApiService>("getPaymentMethods", "GET", "payment-methods")
        assertRoute<CheckoutApiService>("createTransaction", "POST", "transactions")
        assertRoute<CheckoutApiService>("generateQris", "POST", "payments/qris/generate")
        assertRoute<CheckoutApiService>("confirmQris", "POST", "payments/qris/confirm")
        assertRoute<DailyStockApiService>("getDailyStock", "GET", "daily-stock-items")
        assertRoute<DailyStockApiService>("closeSession", "POST", "daily-stock-sessions/close")
        assertRoute<DailyStockApiService>("previewClosing", "POST", "daily-stock-sessions/closing-preview")
        assertRoute<OperationalExpenseApiService>("createExpense", "POST", "cashflow/expenses")

        assertRoute<TransactionsApiService>("getTransactions", "GET", "transactions")
        assertRoute<TransactionsApiService>("getTransactionDetail", "GET", "transactions/{reference}")
        assertRoute<TransactionsApiService>("getTransactionReceiptDetail", "GET", "transactions/{reference}/receipt")
        assertRoute<TransactionsApiService>("getRevenueSummary", "GET", "revenue/summary")
        assertRoute<TransactionsApiService>("voidTransaction", "POST", "transactions/{id}/void")
    }

    @Test
    fun removedSessionCloseRouteIsNotReferenced() {
        val paths = SERVICE_CLASSES.flatMap { service ->
            service.declaredMethods.mapNotNull { method ->
                method.getAnnotation(GET::class.java)?.value
                    ?: method.getAnnotation(POST::class.java)?.value
            }
        }

        assertFalse(paths.any { it == "sessions/{id}/close" })
    }

    private inline fun <reified T> assertRoute(methodName: String, method: String, path: String) {
        val reflectedMethod = T::class.java.declaredMethods.single { it.name == methodName }
        val actual = reflectedMethod.getAnnotation(GET::class.java)?.let { "GET" to it.value }
            ?: reflectedMethod.getAnnotation(POST::class.java)?.let { "POST" to it.value }

        assertEquals(method to path, actual)
    }

    companion object {
        private val SERVICE_CLASSES = listOf(
            AuthApiService::class.java,
            MenuApiService::class.java,
            CheckoutApiService::class.java,
            DailyStockApiService::class.java,
            OperationalExpenseApiService::class.java,
            TransactionsApiService::class.java
        )
    }
}
