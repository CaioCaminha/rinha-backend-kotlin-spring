package com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto

import java.math.BigDecimal
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDto (
    val correlationId: UUID,
    val amount: BigDecimal,
)