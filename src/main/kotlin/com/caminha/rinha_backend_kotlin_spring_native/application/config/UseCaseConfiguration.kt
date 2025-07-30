package com.caminha.rinha_backend_kotlin_spring_native.application.config

import com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient.PaymentProcessorClientGateway
import com.caminha.rinha_backend_kotlin_spring_native.service.PaymentInMemoryRepository
import com.caminha.rinha_backend_kotlin_spring_native.usecase.PaymentsProcessorUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfiguration {

    @Bean
    fun paymentsProcessorUseCase(
        paymentProcessorClientGateway: PaymentProcessorClientGateway,
        paymentInMemoryRepository: PaymentInMemoryRepository,
    ) = PaymentsProcessorUseCase(
        paymentProcessorClientGateway = paymentProcessorClientGateway,
        paymentInMemoryRepository = paymentInMemoryRepository,
    )

}