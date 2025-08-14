package com.caminha.rinha_backend_kotlin_spring_native

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.PaymentHandler
import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.BigDecimalSerializer
import com.caminha.rinha_backend_kotlin_spring_native.utils.serializers.UUIDSerializer
import com.caminha.rinha_backend_kotlin_spring_native.utils.toJsonString
import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.PathHandler
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFlux
class RinhaBackendKotlinSpringNativeApplication

fun main(args: Array<String>) {
	runApplication<RinhaBackendKotlinSpringNativeApplication>(*args).also { context ->
//		val paymentHandler = context.getBean(PaymentHandler::class.java)
//
//		Undertow.builder()
//			.addHttpListener(8091, "0.0.0.0")
//			.setHandler(
//				PathHandler()
//					.addExactPath("/payments") { exchange: HttpServerExchange ->
//						exchange.requestReceiver.receiveFullBytes { _, data ->
//							runBlocking {
//								val stringData = String(data, Charsets.UTF_8).trim()
//
//								println(stringData)
//
//								val correlationId = stringData.substring(23, 59)
//								println(correlationId)
//
//								val amount = stringData.substring(75, 80)
//								println(amount)
//
//								paymentHandler.payments(
//									correlationId = correlationId,
//									amount = amount,
//								)
//							}
//						}
//					}
//			).build().start()
	}
}


@Serializable
data class PaymentRequest (
	@Serializable(with = UUIDSerializer::class)
	val correlationId: UUID,
	@Serializable(with = BigDecimalSerializer::class)
	val amount: BigDecimal,
)

fun PaymentRequest.toPaymentDto() = PaymentDto(
	correlationId = this.correlationId.toString(),
	amount = this.amount.toString(),
)
