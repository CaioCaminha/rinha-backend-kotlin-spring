package com.caminha.rinha_backend_kotlin_spring_native.application.controller

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient.InternalClientGateway
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentSummaryResponse
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentWorkerPool
import com.caminha.rinha_backend_kotlin_spring_native.service.PaymentInMemoryRepository
import com.caminha.rinha_backend_kotlin_spring_native.utils.KotlinSerializationJsonParser
import com.caminha.rinha_backend_kotlin_spring_native.utils.toJsonString
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.logging.Logger
import kotlin.jvm.optionals.getOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait



@Component
class PaymentHandler(
    private val paymentWorkerPool: PaymentWorkerPool,
    private val paymentInMemoryRepository: PaymentInMemoryRepository,
    private val internalClientGateway: InternalClientGateway,
) {

    suspend fun payments(paymentDto: PaymentDto): ServerResponse = coroutineScope {
        println("sending payment to queue")
        paymentWorkerPool.enqueue(paymentDto)

        ServerResponse.ok().build()
            .awaitSingle()
    }

    suspend fun payments(correlationId: String, amount: String): ServerResponse = coroutineScope {
        println("sending payment to queue")
        paymentWorkerPool.enqueue(
            PaymentDto(
                correlationId = UUID.fromString(correlationId),
                amount = BigDecimal(amount),
            )
        )

        ServerResponse.ok().build()
            .awaitSingle()
    }

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

    suspend fun paymentsSummary(
        from: Instant?,
        to: Instant?,
        isInternalCall: Boolean? = false,
    ): PaymentSummaryResponse = coroutineScope {
        Logger.getLogger(PaymentHandler::class.java.name).info("Received payment-summary request")

        if(from?.isAfter(to) == true) {
            ServerResponse.badRequest().bodyValue(
                "Invalid request | from: $from is after to: $to"
            ).awaitSingle()
        }

        println("$from")
        println("$to")

        paymentInMemoryRepository.getSummary(
            from = from,
            to = to,
        ) { from, to ->
            if(
                isInternalCall != true
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


    }

    suspend fun paymentsSummary(request: ServerRequest): ServerResponse = coroutineScope {
        Logger.getLogger(PaymentHandler::class.java.name).info("Received payment-summary request")

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

    suspend fun purgePayments(
        isInternalCall: Boolean = false,
    ): ServerResponse {
        paymentInMemoryRepository.purge(
            isInternalRequest = isInternalCall,
        ) {
            internalClientGateway.purgePayments()
        }

        return ServerResponse.ok().bodyValueAndAwait("Purged Messages")
    }
}
