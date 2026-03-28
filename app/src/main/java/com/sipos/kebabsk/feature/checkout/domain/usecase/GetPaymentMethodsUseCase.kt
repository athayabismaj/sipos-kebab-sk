package com.sipos.kebabsk.feature.checkout.domain.usecase

import com.sipos.kebabsk.feature.checkout.domain.model.PaymentMethod
import com.sipos.kebabsk.feature.checkout.domain.repository.CheckoutRepository

class GetPaymentMethodsUseCase(
    private val checkoutRepository: CheckoutRepository
) {
    suspend operator fun invoke(token: String): Result<List<PaymentMethod>> {
        return checkoutRepository.getPaymentMethods(token)
    }
}
