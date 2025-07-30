package com.caminha.rinha_backend_kotlin_spring_native.domain

import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.BigDecimalSerializer
import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.InstantSerializer
import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.UUIDSerializer
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDetails (
    @Serializable(with = UUIDSerializer::class)
    val correlationId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    @Serializable(with = InstantSerializer::class)
    val requestedAt: Instant = Instant.now(),
    var paymentProcessorType: PaymentProcessorType,
)

enum class PaymentProcessorType {
    DEFAULT, FALLBACK
}