package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.poolworker

import com.caminha.rinha_backend_kotlin_spring_native.application.controller.dto.PaymentDto
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PaymentWorkerPool(
    private val paymentService: PaymentService
) {

    //it's a reference
    private var workerPool: LinkedBlockingQueue<PaymentDto> = LinkedBlockingQueue<PaymentDto>(10)

    init {
        runBlocking(Dispatchers.Default) {
            while(true) {
                workerPool.take().let { payment ->
                    /**
                     * It's launching a new coroutine for each payment inside the worker pool
                     *
                     * This function will be the start point of payment processing
                     * All logic of propagating the payment and storing the details will be done inside a coroutine
                     */
                    launch {
                        // process payment
                    }
                }
            }
        }
    }


    fun enqueue(paymentDto: PaymentDto) {
        if(!workerPool.offer(paymentDto)) {
            throw ArrayIndexOutOfBoundsException(
                "Queue is full" //think of a strategy to work around this
            )
        }
    }

    fun take(): PaymentDto? {
        return workerPool.take()
    }

}