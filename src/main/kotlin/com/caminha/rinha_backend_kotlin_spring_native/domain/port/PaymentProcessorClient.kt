package com.caminha.rinha_backend_kotlin_spring_native.domain.port

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails

interface PaymentProcessorClient {
    suspend fun sendPayment(
        paymentDetails: PaymentDetails
    ): PaymentDetails?
}