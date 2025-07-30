package com.caminha.rinha_backend_kotlin_spring_native.application.controller

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import com.caminha.rinha_backend_kotlin_spring_native.application.gateway.poolworker.PaymentWorkerPoolGateway
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentSummaryResponse
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentWorkerPool
import com.caminha.rinha_backend_kotlin_spring_native.service.PaymentInMemoryRepository
import com.caminha.rinha_backend_kotlin_spring_native.utils.KotlinSerializationJsonParser
import com.caminha.rinha_backend_kotlin_spring_native.utils.toJsonString
import java.math.BigDecimal
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import reactor.core.publisher.Mono

@Component
class PaymentHandler(
    private val paymentWorkerPool: PaymentWorkerPool,
    private val paymentInMemoryRepository: PaymentInMemoryRepository,
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

        println("$from")
        println("$to")

        val paymentSummary = paymentInMemoryRepository.getSummary(
            from = from,
            to = to,
        )

        if(from?.isAfter(to) == true) {
            return ServerResponse.badRequest().bodyValue(
                "Invalid request | from: $from is after to: $to"
            ).awaitSingle()
        }


//        return ServerResponse.ok().body(
//                paymentSummary.toJsonString(),
//                String::class.java
//            ).awaitSingle()
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(paymentSummary.toJsonString())
    }

    suspend fun purge(request: ServerRequest): ServerResponse {
        TODO()
    }
}

fun PaymentDto.toPaymentDetails() = PaymentDetails(
    correlationId = this.correlationId,
    amount = this.amount,
    requestedAt = Instant.now(),
    paymentProcessorType = PaymentProcessorType.DEFAULT
)