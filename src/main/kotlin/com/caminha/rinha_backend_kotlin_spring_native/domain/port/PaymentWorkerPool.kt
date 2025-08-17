package com.caminha.rinha_backend_kotlin_spring_native.domain.port

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto

interface PaymentWorkerPool {
    suspend fun enqueue(paymentDto: PaymentDto)

    suspend fun getPayments(): String
}