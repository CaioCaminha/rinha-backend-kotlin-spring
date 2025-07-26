package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.client.WebClient

class PaymentProcessorClientGateway (
    private val webClient: WebClient,
    @Value("\${webclient.payment-processor.default-api}")
    val defaultPaymentProcessorApi: String,
    @Value("\${webclient.payment-processor.fallback-api}")
    val fallBackPaymentProcessorApi: String,
) {

    suspend fun sendPayment(
        paymentDetails: PaymentDetails
    ) {
        webClient.post()
            .uri("$defaultPaymentProcessorApi/payments")
            .bodyValue(paymentDetails)
            .retrieve()
            .onStatus(
                { it.isError }
            ) { clientResponse ->
                //todo
                /**
                 * Should call fallback API here once it reaches the desired amount of retries?
                 */
                Exception("")
            }

    }

}


data class PaymentProcessorDto(
    val correlationId: UUID,
    val amount: BigDecimal,
    val requestedAt: Instant,
)