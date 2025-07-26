package com.caminha.rinha_backend_kotlin_spring_native.domain

import java.math.BigDecimal
import java.time.Instant

//@Serializable
data class PaymentDetails (
    val correlationId: String,
    val amount: BigDecimal,
    val requestedAt: Instant = Instant.now(),
    val paymentProcessorType: PaymentProcessorType,
)

enum class PaymentProcessorType {
    DEFAULT, FALLBACK
}