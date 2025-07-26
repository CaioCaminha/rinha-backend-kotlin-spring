package com.caminha.rinha_backend_kotlin_spring_native.application.config

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.PaymentHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class RouterFunctionConfig(
    private val paymentHandler: PaymentHandler,
) {

    @Bean
    fun routes(
        paymentHandler: PaymentHandler
    ) : RouterFunction<ServerResponse> {
        return RouterFunctions.route()
            .GET("/payments-summary", paymentHandler::paymentsSummary)
            .POST("/payments", paymentHandler::payments)
            .POST("/purge", paymentHandler::payments)
            .build()
    }

}