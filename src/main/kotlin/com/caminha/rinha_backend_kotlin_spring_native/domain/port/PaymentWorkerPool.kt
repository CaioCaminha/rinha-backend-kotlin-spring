package com.caminha.rinha_backend_kotlin_spring_native.domain.port

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto

interface PaymentWorkerPool {
    fun enqueue(paymentDto: PaymentDto)
}