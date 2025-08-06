package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.utils.toJsonString
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentProcessorClient
import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.BigDecimalSerializer
import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.InstantSerializer
import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.UUIDSerializer
import java.time.Duration
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.Serializable
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono
import reactor.util.retry.Retry

@Component
class PaymentProcessorClientGateway (
    private val webClient: WebClient,
    @Value("\${webclient.payment-processor.default-api}")
    val defaultPaymentProcessorApi: String,
    @Value("\${webclient.payment-processor.fallback-api}")
    val fallBackPaymentProcessorApi: String,
    @Value("\${webclient.payment-processor.max-retries}")
    val maxRetries: Int,
): PaymentProcessorClient {

    override suspend fun sendPayment(
        paymentDetails: PaymentDetails
    ): PaymentDetails? {
        val default =             callPaymentProcessor(
            paymentProcessorApi = defaultPaymentProcessorApi,
            paymentDetails = paymentDetails,
        )
        if(
            default
        ) {
            println("successfully called default payment processor: $paymentDetails")
            return paymentDetails
        } else {
            /**
             * This logic it's not handling when both default and fallback is unavailable
             * Needs to handle this. Maybe return to the queue
             */
            callPaymentProcessor(
                paymentProcessorApi = fallBackPaymentProcessorApi,
                paymentDetails = paymentDetails,
            ).let{
                return if(it == true) {
                    paymentDetails.copy(
                        paymentProcessorType = PaymentProcessorType.FALLBACK
                    )
                } else {
                    null
                }
            }
        }


    }

    private suspend fun callPaymentProcessor(
        paymentProcessorApi: String,
        paymentDetails: PaymentDetails,
    ): Boolean {
        println("sending payment to payment-processor: $paymentProcessorApi")
        return webClient.post()
            .uri("$paymentProcessorApi/payments")
            .header("Content-Type", "application/json")
            .bodyValue(paymentDetails.toJsonString())
            .retrieve()
            .toBodilessEntity()
            .map {
                println("Response status code: ${it.statusCode.value()}")
                it.statusCode.is2xxSuccessful
            }
            .onErrorResume { t ->
                when(t) {
                    is WebClientResponseException -> {
                        println(t)
                        println(t.responseBodyAsString)
                    }
                    else -> println(t.message)
                }
                Mono.just<Boolean>(false)
            }
            .retryWhen(Retry.backoff(maxRetries - 1L, Duration.ofMillis(500)))
            .awaitSingle()
    }



}


fun PaymentDetails.toPaymentProcessorDto() = PaymentProcessorDto(
    correlationId = this.correlationId,
    amount = this.amount,
    requestedAt = this.requestedAt,
)

@Serializable
data class PaymentProcessorDto(
    @Serializable(with = UUIDSerializer::class)
    val correlationId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    @Serializable(with = InstantSerializer::class)
    val requestedAt: Instant,
)