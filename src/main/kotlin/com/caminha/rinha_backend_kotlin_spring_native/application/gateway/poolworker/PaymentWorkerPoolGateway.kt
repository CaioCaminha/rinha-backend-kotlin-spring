package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.poolworker

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentWorkerPool
import com.caminha.rinha_backend_kotlin_spring_native.usecase.PaymentsProcessorUseCase
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class PaymentWorkerPoolGateway(
    private val paymentsProcessorUseCase: PaymentsProcessorUseCase,
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

    private fun CoroutineScope.launchWorker(id: Int) = launch {
        println("Starting worker $id")
        workerPool.consumeEach { payment ->
            println("consuming payment: $payment")
            try{
                withContext(NonCancellable) {
                    paymentsProcessorUseCase.execute(payment.toPaymentDetails())
                }
            } catch (e: Exception) {
                println("failed to process payment | ${e.message}")
            }
        }
    }

    override fun enqueue(paymentDto: PaymentDto) {
        println("adding payment to queue")
        workerPool.trySend(paymentDto)
            .onSuccess {
                println("Payment added to channel $paymentDto")
            }
            .onFailure {
                println("failed add payment to channel $paymentDto | error: ${it?.message}")
            }
    }
}

fun PaymentDto.toPaymentDetails() = PaymentDetails(
    correlationId = UUID.fromString(this.correlationId),
    amount = BigDecimal(this.amount),
    requestedAt = Instant.now(),
    paymentProcessorType = PaymentProcessorType.DEFAULT
)