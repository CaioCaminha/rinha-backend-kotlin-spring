package com.caminha.rinha_backend_kotlin_spring_native.application.config

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.PaymentHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class RouterFunctionConfig {

    @Bean
    fun routes(
        paymentHandler: PaymentHandler
    ) : RouterFunction<ServerResponse> = coRouter {
        POST("/payments", paymentHandler::payments)
        GET("/payments-summary", paymentHandler::paymentsSummary)
        POST("/purge-payments", paymentHandler::purge)
    }

}