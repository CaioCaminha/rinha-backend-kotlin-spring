package com.caminha.rinha_backend_kotlin_spring_native.domain.port

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails

interface PaymentWorkerPool {
    fun enqueue(paymentDetails: PaymentDetails)
}