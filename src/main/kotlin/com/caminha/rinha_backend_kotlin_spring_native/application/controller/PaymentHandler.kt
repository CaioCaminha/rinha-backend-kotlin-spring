package com.caminha.rinha_backend_kotlin_spring_native.application.controller

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import reactor.core.publisher.Mono

@Service
class PaymentHandler(

) {



    suspend fun payments(request: ServerRequest): Mono<ServerResponse> {
        //serializes using kotlinx serialization
        val paymentDto = request.awaitBody<String>().let {
            Json.decodeFromString<PaymentDto>(it)
        }


        return ServerResponse.ok().build()
    }

    fun paymentsSummary(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body { outputMessage, context ->

        }
    }






}