package com.caminha.rinha_backend_kotlin_spring_native.usecase

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentProcessorClient
import com.caminha.rinha_backend_kotlin_spring_native.service.PaymentInMemoryRepository
import java.time.Instant
import kotlinx.coroutines.coroutineScope

class PaymentsProcessorUseCase(
    private val paymentProcessorClientGateway: PaymentProcessorClient,
    private val paymentInMemoryRepository: PaymentInMemoryRepository,
) {

    /**
     * Propagate the payment to payment-processor
     * Save the information on the database
     */
    suspend fun execute(
        paymentDetails: PaymentDetails,
    ) = coroutineScope {
        println("Started PaymentsProcessorUseCase | $paymentDetails")
        paymentProcessorClientGateway.sendPayment(
            paymentDetails = paymentDetails,
        )?.let { savedPayment ->
            println("Payment processed: ${savedPayment.amount}")
            paymentInMemoryRepository.addPayment(savedPayment)
        }
    }

}

