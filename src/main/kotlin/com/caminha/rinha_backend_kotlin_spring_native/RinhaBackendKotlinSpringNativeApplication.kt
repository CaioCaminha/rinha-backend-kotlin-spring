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
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse

@SpringBootApplication(
	exclude = [
		ServletWebServerFactoryAutoConfiguration::class,
		ReactiveWebServerFactoryAutoConfiguration::class,
	]
)
class RinhaBackendKotlinSpringNativeApplication


/**
 * Check performance improvement Using Undertow instead of webflux
 */

fun main(args: Array<String>) {
	val application = SpringApplication(RinhaBackendKotlinSpringNativeApplication::class.java)
	application.webApplicationType = WebApplicationType.NONE
	application.run(*args).also { context ->
		val paymentHandler = context.getBean(PaymentHandler::class.java)
		val env = context.getBean(Environment::class.java)

		val port = env.getProperty("server.port", Int::class.java) ?: 8080

		Undertow.builder()
			.addHttpListener(port, "0.0.0.0")
			.setHandler(
				PathHandler()
					.addExactPath("/payments") { exchange: HttpServerExchange ->
						exchange.requestReceiver.receiveFullBytes { _, data ->
							runBlocking {
								val stringData = String(data, Charsets.UTF_8).trim()

								println(stringData)

								val correlationId = stringData.substring(23, 59)
								println(correlationId)

								val amount = stringData.substring(75, 80)
								println(amount)

								paymentHandler.payments(
									correlationId = correlationId,
									amount = amount,
								)
							}
						}
					}
					.addExactPath("/payments-summary") { exchange: HttpServerExchange ->
						runBlocking {
							val from = exchange.queryParameters.get("from")?.firstOrNull()?.let { Instant.parse(it) }
							val to = exchange.queryParameters.get("to")?.firstOrNull()?.let { Instant.parse(it) }
							val isInternalCall = exchange.requestHeaders.get("isInternalCall")?.firstOrNull()

							val response = paymentHandler.paymentsSummary(
								from = from,
								to = to,
								isInternalCall = isInternalCall.toBoolean(),
							)
							exchange.responseSender.send(response.toJsonString())
						}
					}
					.addExactPath("/purge-payments") { exchange: HttpServerExchange ->
						runBlocking {
							paymentHandler.purgePayments(
								isInternalCall = exchange.requestHeaders.get("isInternalCall")?.firstOrNull().toBoolean(),
							)
						}
					}
			).build().start()
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
