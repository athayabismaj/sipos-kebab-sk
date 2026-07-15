package com.sipos.kebabsk.feature.checkout.data.repository

import com.google.gson.Gson
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.checkout.data.remote.CreateTransactionRequest
import com.sipos.kebabsk.feature.checkout.data.remote.CreateTransactionResponse
import com.sipos.kebabsk.feature.checkout.data.remote.PaymentMethodsResponse
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutItemInput
import com.sipos.kebabsk.testutil.ContractFixtureLoader
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class CheckoutContractFixtureTest {
    private val gson = Gson()

    @Test
    fun checkoutRequestUsesBackendFieldNamesAndDoesNotSendBranchId() = runTest {
        val api = FixtureCheckoutApiService(
            successBody = fixtureResponse("checkout_success.json")
        )
        val repository = CheckoutRepositoryImpl(api)

        val result = repository.createTransaction(
            token = "fixture-token",
            request = CheckoutRequestData(
                paymentMethodId = 1L,
                paidAmount = 15_000L,
                items = listOf(CheckoutItemInput(variantId = 401L, qty = 1)),
                note = null
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("Bearer fixture-token", api.authorization)
        val payload = gson.toJsonTree(api.request).asJsonObject
        val expected = ContractFixtureLoader.jsonObject("checkout_request.json")
        assertEquals(expected, payload)
        assertFalse(payload.has("branch_id"))
    }

    @Test
    fun checkoutSuccessMapsCoreReceiptValuesAndIgnoresAdditionalFieldsSafely() = runTest {
        val repository = CheckoutRepositoryImpl(
            FixtureCheckoutApiService(successBody = fixtureResponse("checkout_success.json"))
        )

        val result = repository.createTransaction("token", sampleRequest()).getOrThrow()

        assertEquals(1001L, result.transactionId)
        assertEquals("TRX-FIX-20260715-001", result.transactionCode)
        assertEquals(10_000L, result.totalAmount)
        assertEquals(15_000L, result.paidAmount)
        assertEquals(5_000L, result.changeAmount)
    }

    @Test
    fun checkoutValidationAndStockConflictKeepBackendBusinessMessages() = runTest {
        val validation = CheckoutRepositoryImpl(
            FixtureCheckoutApiService(errorFixture = "checkout_validation_error.json", errorCode = 422)
        ).createTransaction("token", sampleRequest())
        val stockConflict = CheckoutRepositoryImpl(
            FixtureCheckoutApiService(errorFixture = "checkout_stock_conflict.json", errorCode = 422)
        ).createTransaction("token", sampleRequest())

        assertTrue(validation.isFailure)
        assertEquals("Validasi transaksi tidak valid.", validation.exceptionOrNull()?.message)
        assertTrue(stockConflict.isFailure)
        assertEquals(
            "Variant 'Mini' tidak tersedia untuk dijual.",
            stockConflict.exceptionOrNull()?.message
        )
    }

    @Test
    fun paymentMethodFixtureMapsCashWithLongId() = runTest {
        val paymentBody = gson.fromJson(
            ContractFixtureLoader.jsonObject("payment_methods_success.json"),
            PaymentMethodsResponse::class.java
        )
        val api = FixtureCheckoutApiService(paymentMethodsBody = paymentBody)

        val methods = CheckoutRepositoryImpl(api).getPaymentMethods("fixture-token").getOrThrow()

        assertEquals("Bearer fixture-token", api.paymentAuthorization)
        assertEquals(1, methods.size)
        assertEquals(1L, methods.single().id)
        assertEquals("Cash", methods.single().name)
    }

    private fun fixtureResponse(name: String): CreateTransactionResponse {
        return gson.fromJson(ContractFixtureLoader.jsonObject(name), CreateTransactionResponse::class.java)
    }

    private fun sampleRequest() = CheckoutRequestData(
        paymentMethodId = 1L,
        paidAmount = 15_000L,
        items = listOf(CheckoutItemInput(variantId = 401L, qty = 1)),
        note = null
    )
}

private class FixtureCheckoutApiService(
    private val successBody: CreateTransactionResponse? = null,
    private val errorFixture: String? = null,
    private val errorCode: Int = 422,
    private val paymentMethodsBody: PaymentMethodsResponse? = null
) : CheckoutApiService {
    var authorization: String? = null
    var paymentAuthorization: String? = null
    var request: CreateTransactionRequest? = null

    override suspend fun createTransaction(
        authorization: String,
        request: CreateTransactionRequest
    ): Response<CreateTransactionResponse> {
        this.authorization = authorization
        this.request = request
        successBody?.let { return Response.success(it) }
        val raw = ContractFixtureLoader.jsonObject(requireNotNull(errorFixture)).toString()
        return Response.error(errorCode, raw.toResponseBody("application/json".toMediaType()))
    }

    override suspend fun getPaymentMethods(authorization: String): Response<PaymentMethodsResponse> {
        paymentAuthorization = authorization
        return Response.success(requireNotNull(paymentMethodsBody))
    }
}
