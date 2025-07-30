package com.caminha.rinha_backend_kotlin_spring_native.service

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentSummary
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentSummaryResponse
import com.caminha.rinha_backend_kotlin_spring_native.utils.toJsonString
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.springframework.stereotype.Repository

/**
 * This service will be saving on an inMemory collection (Array, List, Set)
 * Need to think of a strategy to keep memory usage low (Saving only the amount processed by default / fallback api)
 *
 * Adds a overhead of sync between the other instances of this application.
 * And each time I receive /payments-summary I need to send another http request to the other backend to sync data
 * With only 2 instances it's easy, but what if it grows to more than 3 instances, how can I sync this?
 */
@Repository
class PaymentInMemoryRepository {

    /**
     * The Idea is to save as less information as possible, in this case will be two data structures
     * holding the totalRequests and the totalAmount processed.
     *
     * This Repository will be called from multiple coroutines and needs to be "thread-safe" in order to avoid data races
     * and data inconsistency
     */
    private val payments = CopyOnWriteArrayList<PaymentDetails>()

    private val defaultPaymentSummaryResults = PaymentSummaryResults()
    private val fallbackPaymentsSummaryResults = PaymentSummaryResults()

    /**
     * Each instance will manage its own local storage
     * Need to develop a synchronization strategy:
     *
     * Current Strategy: Everytime we receive a payments-summary we need to send an http request to the other
     * instance and merge the values
     */

    fun addPayment(
        paymentDetails: PaymentDetails,
    ) {
        if(payments.add(paymentDetails)) {
            when(paymentDetails.paymentProcessorType) {
                PaymentProcessorType.DEFAULT -> defaultPaymentSummaryResults.incrementValues(
                    amount = paymentDetails.amount,
                )
                PaymentProcessorType.FALLBACK -> fallbackPaymentsSummaryResults.incrementValues(
                    amount = paymentDetails.amount,
                )
            }
        } else {
            println("Failed to save payment: $paymentDetails")
        }
    }

    fun getSummary(
        from: Instant? = null,
        to: Instant? = null,
        syncBlock: (suspend () -> List<PaymentDetails>)? = null,
    ): PaymentSummaryResponse {
        //need to count totalRequests and totalAmount for DEFAULT and for FALLBACK

        if(from != null && to != null) {
            val paymentsByType = payments.filter { details ->
                details.requestedAt.isAfter(from) &&
                        details.requestedAt.isBefore(to)
            }.groupBy { it.paymentProcessorType }

            println("paymentsByType: $paymentsByType")

            val defaultAmount = AtomicReference<BigDecimal>(BigDecimal.ZERO)

            paymentsByType[PaymentProcessorType.DEFAULT]
                ?.forEach { t -> defaultAmount.set(defaultAmount.get().plus(t.amount)) }

            val fallBackAmount = AtomicReference<BigDecimal>(BigDecimal.ZERO)

            paymentsByType[PaymentProcessorType.FALLBACK]
                ?.forEach { t -> fallBackAmount.set(fallBackAmount.get().plus(t.amount)) }

            return PaymentSummaryResponse(
                default = PaymentSummary(
                    totalAmount = defaultAmount.get(),
                    totalRequests = paymentsByType[PaymentProcessorType.DEFAULT]?.count() ?: 0,
                ),
                fallback = PaymentSummary(
                    totalAmount = fallBackAmount.get(),
                    totalRequests = paymentsByType[PaymentProcessorType.FALLBACK]?.count() ?: 0,
                )
            )
        } else {
            return PaymentSummaryResponse(
                default = defaultPaymentSummaryResults.toPaymentSummary(),
                fallback = fallbackPaymentsSummaryResults.toPaymentSummary()
            )
        }
    }
}


data class PaymentSummaryResults(
    val totalRequests: AtomicInteger,
    val totalAmountAtomicReference: AtomicReference<BigDecimal>,
){
    constructor() : this(
        AtomicInteger(0),
        AtomicReference(BigDecimal.ZERO),
    )

    fun incrementValues(
        amount: BigDecimal,
    ) {
        totalRequests.incrementAndGet()
        totalAmountAtomicReference.set(totalAmountAtomicReference.get().plus(amount))
    }

    fun toPaymentSummary() = PaymentSummary(
        totalRequests = this.totalRequests.get(),
        totalAmount = this.totalAmountAtomicReference.get(),
    )
}