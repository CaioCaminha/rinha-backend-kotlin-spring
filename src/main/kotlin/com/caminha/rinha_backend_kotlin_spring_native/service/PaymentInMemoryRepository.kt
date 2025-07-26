package com.caminha.rinha_backend_kotlin_spring_native.service

import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentDetails
import com.caminha.rinha_backend_kotlin_spring_native.domain.PaymentProcessorType
import java.math.BigDecimal
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

    private val defaultPaymentSummaryResults = PaymentSummaryResults()

    private val fallbackPaymentsSummaryResults = PaymentSummaryResults()


    fun addPayment(
        paymentDetails: PaymentDetails,
    ) {
        when(paymentDetails.paymentProcessorType) {
            PaymentProcessorType.DEFAULT -> defaultPaymentSummaryResults.incrementValues(
                amount = paymentDetails.amount,
            )
            PaymentProcessorType.FALLBACK -> fallbackPaymentsSummaryResults.incrementValues(
                amount = paymentDetails.amount,
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
}