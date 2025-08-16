package com.caminha.rinha_backend_kotlin_spring_native.usecase

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentProcessorClient
import com.caminha.rinha_backend_kotlin_spring_native.service.PaymentInMemoryRepository
import java.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PaymentsProcessorUseCase(

) {

    /**
     * Propagate the payment to payment-processor
     * Save the information on the database
     *
     * It's processing the same payment twice
     *
     * Not sure if the same event it's been consumed by different workers
     * Or if it's calling twice for the same payment
     */
    suspend fun execute(
        paymentDetails: PaymentDetails,
    ) = coroutineScope {
        println("Started PaymentsProcessorUseCase | correlationId: ${paymentDetails.correlationId}")

    }

}

