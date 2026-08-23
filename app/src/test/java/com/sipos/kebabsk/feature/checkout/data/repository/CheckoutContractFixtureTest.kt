package com.sipos.kebabsk.feature.checkout.data.repository

import com.google.gson.Gson
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.checkout.data.remote.CreateTransactionRequest
import com.sipos.kebabsk.feature.checkout.data.remote.CreateTransactionResponse
import com.sipos.kebabsk.feature.checkout.data.remote.ConfirmQrisRequest
import com.sipos.kebabsk.feature.checkout.data.remote.ConfirmQrisResponse
import com.sipos.kebabsk.feature.checkout.data.remote.GenerateQrisRequest
import com.sipos.kebabsk.feature.checkout.data.remote.GenerateQrisResponse
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
        assertEquals("Jl. Kampus UMK, Kudus", result.branchAddress)
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
    fun paymentMethodFixtureMapsCashAndQrisWithLongIds() = runTest {
        val paymentBody = gson.fromJson(
            ContractFixtureLoader.jsonObject("payment_methods_success.json"),
            PaymentMethodsResponse::class.java
        )
        val api = FixtureCheckoutApiService(paymentMethodsBody = paymentBody)

        val methods = CheckoutRepositoryImpl(api).getPaymentMethods("fixture-token").getOrThrow()

        assertEquals("Bearer fixture-token", api.paymentAuthorization)
        assertEquals(listOf(1L, 2L), methods.map { it.id })
        assertEquals(listOf("Cash", "QRIS"), methods.map { it.name })
    }

    @Test
    fun qrisGenerateFixtureMapsDynamicPayloadAndAmount() = runTest {
        val qrisBody = gson.fromJson(
            ContractFixtureLoader.jsonObject("qris_generate_success.json"),
            GenerateQrisResponse::class.java
        )
        val api = FixtureCheckoutApiService(qrisBody = qrisBody)

        val qris = CheckoutRepositoryImpl(api).generateQris("fixture-token", 1001L).getOrThrow()

        assertEquals("Bearer fixture-token", api.qrisAuthorization)
        assertEquals(1001L, api.qrisRequest?.transactionId)
        assertEquals("SK Kebab Pekeng", qris.merchantName)
        assertEquals(10_000L, qris.amount)
        assertEquals("QRS-ABCDEFGHIJKLMNOPQRST", qris.reference)
        assertEquals("2026-08-21T12:05:00+07:00", qris.expiresAt)
        assertTrue(qris.payload.startsWith("000201010212"))
    }

    @Test
    fun qrisConfirmFixtureMapsServerConfirmationAndReference() = runTest {
        val confirmBody = gson.fromJson(
            ContractFixtureLoader.jsonObject("qris_confirm_success.json"),
            ConfirmQrisResponse::class.java
        )
        val api = FixtureCheckoutApiService(confirmBody = confirmBody)

        val confirmation = CheckoutRepositoryImpl(api)
            .confirmQris("fixture-token", 1001L, "QRS-ABCDEFGHIJKLMNOPQRST")
            .getOrThrow()

        assertEquals("Bearer fixture-token", api.confirmAuthorization)
        assertEquals(1001L, api.confirmRequest?.transactionId)
        assertEquals("QRS-ABCDEFGHIJKLMNOPQRST", api.confirmRequest?.qrisReference)
        assertEquals("SUCCESS", confirmation.status)
        assertEquals(10_000L, confirmation.amount)
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
    private val paymentMethodsBody: PaymentMethodsResponse? = null,
    private val qrisBody: GenerateQrisResponse? = null,
    private val confirmBody: ConfirmQrisResponse? = null
) : CheckoutApiService {
    var authorization: String? = null
    var paymentAuthorization: String? = null
    var request: CreateTransactionRequest? = null
    var qrisAuthorization: String? = null
    var qrisRequest: GenerateQrisRequest? = null
    var confirmAuthorization: String? = null
    var confirmRequest: ConfirmQrisRequest? = null

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

    override suspend fun generateQris(
        authorization: String,
        request: GenerateQrisRequest
    ): Response<GenerateQrisResponse> {
        qrisAuthorization = authorization
        qrisRequest = request
        return Response.success(requireNotNull(qrisBody))
    }

    override suspend fun confirmQris(
        authorization: String,
        request: ConfirmQrisRequest
    ): Response<ConfirmQrisResponse> {
        confirmAuthorization = authorization
        confirmRequest = request
        return Response.success(requireNotNull(confirmBody))
    }
}
