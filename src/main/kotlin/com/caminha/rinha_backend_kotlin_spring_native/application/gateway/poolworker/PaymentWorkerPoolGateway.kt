package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.poolworker

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.port.PaymentWorkerPool
import com.caminha.rinha_backend_kotlin_spring_native.usecase.PaymentsProcessorUseCase
import jakarta.annotation.PostConstruct
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class PaymentWorkerPoolGateway(
    private val paymentsProcessorUseCase: PaymentsProcessorUseCase,
): PaymentWorkerPool {




    //it's a reference
    private val workerPool = Channel<PaymentDetails>(capacity = 9000)
//    private val workerCount: Int = Runtime.getRuntime().availableProcessors()
    private val workerCount: Int = 16

    /**
     * Adding a runBlocking inside an init block will block the current thread
     * Causing initialization issues
     */

//    init {
//        runBlocking {...}
//    }

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
                    paymentsProcessorUseCase.execute(payment)
                }
            } catch (e: Exception) {
                println("failed to process payment | ${e.message}")
            }
        }
    }

    override fun enqueue(paymentDetails: PaymentDetails) {
        println("adding payment to queue")
        workerPool.trySend(paymentDetails)
            .onSuccess {
                println("Payment added to channel $paymentDetails")
            }
            .onFailure {
                println("failed add payment to channel $paymentDetails | error: ${it?.message}")
            }
    }


}