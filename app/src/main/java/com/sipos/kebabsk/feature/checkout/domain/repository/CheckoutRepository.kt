package com.sipos.kebabsk.feature.checkout.domain.repository

import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutResult
import com.sipos.kebabsk.feature.checkout.domain.model.PaymentMethod
import com.sipos.kebabsk.feature.checkout.domain.model.QrisPayment

interface CheckoutRepository {
    suspend fun getPaymentMethods(token: String): Result<List<PaymentMethod>>
    suspend fun createTransaction(token: String, request: CheckoutRequestData): Result<CheckoutResult>
    suspend fun generateQris(token: String, transactionId: Long): Result<QrisPayment>
}
