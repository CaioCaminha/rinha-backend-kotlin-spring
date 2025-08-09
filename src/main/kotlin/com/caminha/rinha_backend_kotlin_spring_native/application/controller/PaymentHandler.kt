package com.caminha.rinha_backend_kotlin_spring_native.application.controller

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient.InternalClientGateway
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentWorkerPool
import com.caminha.rinha_backend_kotlin_spring_native.service.PaymentInMemoryRepository
import com.caminha.rinha_backend_kotlin_spring_native.utils.KotlinSerializationJsonParser
import com.caminha.rinha_backend_kotlin_spring_native.utils.toJsonString
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Component
class PaymentHandler(
    private val paymentWorkerPool: PaymentWorkerPool,
    private val paymentInMemoryRepository: PaymentInMemoryRepository,
    private val internalClientGateway: InternalClientGateway,
) {

    /**
     * Must check if this code it's actually running concurrently
     * Meaning, Following the pattern of one coroutine per request.
     */
     suspend fun payments(request: ServerRequest): ServerResponse = coroutineScope {
        //serializes using kotlinx serialization
        val paymentDto = request.awaitBody<String>().let {
            KotlinSerializationJsonParser
                .DEFAULT_KOTLIN_SERIALIZATION_PARSER
                .decodeFromString<PaymentDto>(it)
        }

         println("sending payment to queue")
         paymentWorkerPool.enqueue(paymentDto)

        ServerResponse.ok().build()
            .awaitSingle()
    }

    suspend fun paymentsSummary(request: ServerRequest): ServerResponse = coroutineScope {


        val from = request.queryParam("from").getOrNull()?.let { string -> Instant.parse(string) }
        val to = request.queryParam("to").getOrNull()?.let { string -> Instant.parse(string) }

        if(from?.isAfter(to) == true) {
            ServerResponse.badRequest().bodyValue(
                "Invalid request | from: $from is after to: $to"
            ).awaitSingle()
        }

        println("$from")
        println("$to")

        val paymentSummaryResponse = paymentInMemoryRepository.getSummary(
            from = from,
            to = to,
        ) { from, to ->
            if(
                request.headers().firstHeader("isInternalCall") == null
            ) {
                println("calling internal payment-summary to sync and merge data")

                internalClientGateway.getPaymentsSummary(
                    from = from,
                    to = to
                )
            } else {
                println("Not calling internal payment-summary")
                null
            }
        }

        ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(paymentSummaryResponse.toJsonString())
            .awaitSingle()
    }

    suspend fun purgePayments(request: ServerRequest): ServerResponse {
        val isInternal = request.queryParam("internalRequest").isPresent

        paymentInMemoryRepository.purge(
            isInternalRequest = isInternal,
        ) {
            internalClientGateway.purgePayments()
        }

        return ServerResponse.ok().bodyValueAndAwait("Purged Messages")
    }
}
