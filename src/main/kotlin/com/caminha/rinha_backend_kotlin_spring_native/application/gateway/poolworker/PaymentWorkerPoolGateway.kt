package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.poolworker

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentProcessorClient
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentWorkerPool
import com.caminha.rinha_backend_kotlin_spring_native.service.PaymentInMemoryRepository
import com.caminha.rinha_backend_kotlin_spring_native.utils.toJsonString
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class PaymentWorkerPoolGateway(
    private val paymentProcessorClientGateway: PaymentProcessorClient,
    private val paymentInMemoryRepository: PaymentInMemoryRepository,
): PaymentWorkerPool {


    /**
     * Check if Channel it's the best option
     * If there isn't any bottleneck added for using Channel
     */
    private val workerPool = Channel<PaymentDto>(capacity = 20000)

    /**
     * Having more coroutines than available Cores or Threads to execute them
     * brings no benefit, maybe it creates more overhead due to context switching.
     *
     * Study about coroutine builders to better understand this process
     */
    private val workerCount: Int = Runtime.getRuntime().availableProcessors()

    /**
     * Adding a runBlocking inside an init block will block the current thread
     * Causing initialization issues
     */

    @EventListener(ApplicationReadyEvent::class)
    suspend fun startProcessing() = coroutineScope {
        //getting available processors
        repeat(workerCount) { id ->
            launchWorker(id)
        }
    }

    override suspend fun getPayments(): String {
        return buildJsonObject {
            put("numberOfPayments", JsonPrimitive(workerPool.toList().count()))
        }.toJsonString()
    }

    private fun CoroutineScope.launchWorker(id: Int) = launch(NonCancellable) {
        println("Starting worker $id")
        while (true) {
            try{
                /**
                 * It's calling payment-processor async creating a new coroutine
                 * But it's calling paymentRepository sequentially
                 */
                val payment = workerPool.receive()

                async {
                    paymentProcessorClientGateway.sendPayment(payment.toPaymentDetails())
                }.await()?.let{ payment: PaymentDetails ->
                    paymentInMemoryRepository.addPayment(payment)
                }
            } catch (e: Exception) {
                println("failed to process payment | ${e.message}")
            }
        }
    }

    override suspend fun enqueue(paymentDto: PaymentDto): Unit = coroutineScope {
        println("adding payment to queue")
        workerPool.trySend(paymentDto)
            .onSuccess {
                println("Payment added to channel ${paymentDto.correlationId}")
            }
            .onFailure {
                println("failed add payment to channel ${paymentDto.correlationId} | error: ${it?.message}")
            }
    }
}

fun PaymentDto.toPaymentDetails() = PaymentDetails(
    correlationId = this.correlationId,
    amount = this.amount,
    requestedAt = Instant.now(),
    paymentProcessorType = PaymentProcessorType.DEFAULT
)