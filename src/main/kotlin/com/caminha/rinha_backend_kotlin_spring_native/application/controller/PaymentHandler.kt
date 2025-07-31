package com.caminha.rinha_backend_kotlin_spring_native.application.controller

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient.InternalClientGateway
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentWorkerPool
import com.caminha.rinha_backend_kotlin_spring_native.service.PaymentInMemoryRepository
import com.caminha.rinha_backend_kotlin_spring_native.utils.KotlinSerializationJsonParser
import com.caminha.rinha_backend_kotlin_spring_native.utils.toJsonString
import java.time.Instant
import kotlin.jvm.optionals.getOrNull
import kotlinx.coroutines.reactor.awaitSingle
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

     suspend fun payments(request: ServerRequest): ServerResponse {
        //serializes using kotlinx serialization
        val paymentDto = request.awaitBody<String>().let {
            KotlinSerializationJsonParser
                .DEFAULT_KOTLIN_SERIALIZATION_PARSER
                .decodeFromString<PaymentDto>(it)
        }

         println("sending payment to queue")
         paymentWorkerPool.enqueue(paymentDto.toPaymentDetails())

        return ServerResponse.ok().build()
            .awaitSingle()
    }

    suspend fun paymentsSummary(request: ServerRequest): ServerResponse {


        val from = request.queryParam("from").getOrNull()?.let { string -> Instant.parse(string) }
        val to = request.queryParam("to").getOrNull()?.let { string -> Instant.parse(string) }

        if(from?.isAfter(to) == true) {
            return ServerResponse.badRequest().bodyValue(
                "Invalid request | from: $from is after to: $to"
            ).awaitSingle()
        }

        println("$from")
        println("$to")

        val paymentSummaryResponse = paymentInMemoryRepository.getSummary(
            from = from,
            to = to,
        ) { from, to ->
            internalClientGateway.getPaymentsSummary(
                from = from,
                to = to
            )
        }

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValueAndAwait(paymentSummaryResponse.toJsonString())
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

fun PaymentDto.toPaymentDetails() = PaymentDetails(
    correlationId = this.correlationId,
    amount = this.amount,
    requestedAt = Instant.now(),
    paymentProcessorType = PaymentProcessorType.DEFAULT
)