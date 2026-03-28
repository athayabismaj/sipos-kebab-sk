package com.sipos.kebabsk.feature.checkout.domain.usecase

import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutResult
import com.sipos.kebabsk.feature.checkout.domain.repository.CheckoutRepository

class CreateTransactionUseCase(
    private val checkoutRepository: CheckoutRepository
) {
    suspend operator fun invoke(token: String, request: CheckoutRequestData): Result<CheckoutResult> {
        return checkoutRepository.createTransaction(token, request)
    }
}
