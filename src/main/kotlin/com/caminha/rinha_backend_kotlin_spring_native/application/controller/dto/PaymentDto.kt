package com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto

import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.BigDecimalSerializer
import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.UUIDSerializer
import java.math.BigDecimal
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDto (
    @Serializable(with = UUIDSerializer::class)
    val correlationId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
)

