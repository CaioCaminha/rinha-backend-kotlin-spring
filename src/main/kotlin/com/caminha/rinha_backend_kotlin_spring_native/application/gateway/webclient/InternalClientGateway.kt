package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentSummaryResponse
import com.caminha.rinha_backend_kotlin_spring_native.utils.KotlinSerializationJsonParser
import java.time.Instant
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Component
class InternalClientGateway(
    private val webClient: WebClient,
    @Value("\${webclient.internal.url}")
    val internalApi: String,
) {

    suspend fun getPaymentsSummary(
        from: Instant?,
        to: Instant?,
    ): PaymentSummaryResponse {
        println("calling internalApi: $internalApi")
        val response = webClient.get()
            .let {
                if(from != null && to != null) {
                    it.uri("$internalApi/payments-summary?from=$from&to=$to")
                } else {
                    it.uri("$internalApi/payments-summary")
                }
            }
            .header("Content-Type", "application/json")
            .header("isInternalCall", "true")
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingleOrNull()
        println("Response from payments-summary internal: $response")
        return KotlinSerializationJsonParser
            .DEFAULT_KOTLIN_SERIALIZATION_PARSER
            .decodeFromString(response ?: throw RuntimeException("Error parsing"))
    }

    suspend fun purgePayments() {
        webClient.post()
            .uri("$internalApi/purge-payments?internalRequest=true")
            .retrieve()
            .toBodilessEntity()
            .awaitSingle()
    }


}