package com.caminha.rinha_backend_kotlin_spring_native.domain

import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
data class PaymentSummaryResponse (
    val default: PaymentSummary,
    val fallback: PaymentSummary,
)

@Serializable
data class PaymentSummary(
    val totalRequests: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val totalAmount: BigDecimal,
)
