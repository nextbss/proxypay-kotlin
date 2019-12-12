/*
 *
 *  * Copyright (C) Next Solutions - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential and will be punished by law
 *  * Written by Alexandre Antonio Juca <alexandre.juca@nextbss.co.ao>
 *
 */

package co.ao.box

import com.squareup.moshi.JsonAdapter
import co.ao.box.client.TransactionCallback
import co.ao.box.config.ProxyPayConfig
import co.ao.box.core.ApiResponseHandler
import co.ao.box.models.MockPaymentRequest
import co.ao.box.models.MockPaymentResponse
import co.ao.box.models.PaymentReferenceRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This class is responsible for interacting wth references
 * in the ProxyPay system. The methods that this class provides
 * can create references and alter the state of existing references.
 */
class ProxyPayPayment(builder: PaymentTransactionBuilder) : ProxyPay() {
    private val jsonAdapter: JsonAdapter<String> = moshi.adapter(String::class.java)
    private val referenceAdapter: JsonAdapter<String> = moshi.adapter(String::class.java)
    private val mockPaymentAdapter: JsonAdapter<MockPaymentResponse> = moshi.adapter(MockPaymentResponse::class.java)
    private val logger = Logger.getLogger(this.javaClass.simpleName)

    init {
        super.config = builder.config
        if (builder.request != null) {
            super.referenceRequest = builder.request!!
        }

        if (builder.mockPaymentRequest != null) {
            super.mockPaymentRequest = builder.mockPaymentRequest!!
        }
    }

    class PaymentTransactionBuilder {
        var request: PaymentReferenceRequest? = null
        lateinit var config: ProxyPayConfig
        var mockPaymentRequest: MockPaymentRequest? = null

        /**
         * This method is used to add a PaymentReferenceRequest that will be sent
         * to ProxyPay to create or alter a payment reference
         */
        fun addReferenceRequest(request: PaymentReferenceRequest): PaymentTransactionBuilder {
            this.request = request
            if (!validDate(request.end_datetime!!)) {
                throw IllegalArgumentException("Date is mal-formatted. Should be in YYYY-MM-dd format. ")
            }

            if (dateIsInPast(request.end_datetime!!)) {
                throw java.lang.IllegalArgumentException("Invalid date. Date must be in the future.")
            }
            return this
        }

        fun addProxyPayConfiguration(config: ProxyPayConfig): PaymentTransactionBuilder {
            this.config = config
            return this
        }

        fun addMockPaymentRequest(request: MockPaymentRequest): PaymentTransactionBuilder {
            this.mockPaymentRequest = request
            return this
        }

        fun build(): ProxyPayPayment {
            return ProxyPayPayment(this)
        }
    }

    /**
     * Creates or updates a payment reference with given Id
     * @param callback: TransactionCallback<PaymentReferenceResponse>
     * @param id: String - Valid Reference Id
     * @throws IllegalStateException - Throws exception when no reference id is provided
     */
    fun generateReference(callback: TransactionCallback<String>, id: String) {
        logger.log(Level.INFO, "Initiated request to generate MULTI-CAIXA reference")
        if (id.isBlank()) throw IllegalStateException("You must provide a valid and existing payment reference id.")
        prepareRequest("/references/$id", "put", this.referenceRequest, sendBodyInRequest = true)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                ApiResponseHandler.handleResponse(jsonAdapter, response, callback)
            }

            override fun onFailure(call: Call, exception: IOException) {
                println(exception.message)
            }
        })
    }

    /**
     *  Allows the generation of a Reference Id
     * @param callback: TransactionCallback - The callback to trigger events
     */
    fun generateReferenceId(callback: TransactionCallback<String>) {
        logger.log(Level.INFO, "Initiated request to generate reference ID via MULTI-CAIXA")
        prepareRequest("/reference_ids", "post", sendBodyInRequest = false)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                ApiResponseHandler.handleResponse(referenceAdapter, response, callback)
            }

            override fun onFailure(call: Call, exception: IOException) {
                println(exception.message)
            }
        })
    }

    fun mockPayment(callback: TransactionCallback<MockPaymentResponse>) {
        logger.log(Level.INFO, "Initiated request to mock/simulate payments in SANDBOX environment")
        prepareMockRequest("/payments", "post", this.mockPaymentRequest)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                ApiResponseHandler.handleMockPaymentResponse(mockPaymentAdapter, response, callback)
            }

            override fun onFailure(call: Call, exception: IOException) {
                println(exception.message)
            }
        })
    }

    /**
     * Deletes a reference with a given Id
     * @param callback: TransactionCallback<String>
     * @param id: String - Valid Reference Id
     * @throws IllegalStateException - Throws exception when no reference id is provided
     */
    fun deleteReference(transactionCallback: TransactionCallback<String>, id: String) {
        logger.log(Level.INFO, "Initiated request to delete reference")
        if (id.isBlank()) throw IllegalStateException("You must provide a valid and existing payment reference id.")
        prepareRequest("/references/$id", "delete", this.referenceRequest, sendBodyInRequest = false)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(p0: Call, response: Response) {
                ApiResponseHandler.handleResponse(jsonAdapter, response, transactionCallback)
            }

            override fun onFailure(call: Call, exception: IOException) {
                println(exception.message)
            }
        })
    }

    /**
     * Acknowledge that a payment was processed, after being retrieved by getPayments() and discards it from the sever.
     * @param callback: TransactionCallback<String>
     * @param id: String - Valid Reference Id
     * @throws IllegalStateException - Throws exception when no reference id is provided
     */
    fun acknowledgePayment(transactionCallback: TransactionCallback<String>, id: String) {
        logger.log(Level.INFO, "Initiated request to acknowledge payment")
        if (id.isBlank()) throw IllegalStateException("You must provide a valid and existing payment reference id.")
        prepareRequest("/payments/$id", "delete", this.referenceRequest, sendBodyInRequest = false)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(p0: Call, p1: Response) {
                ApiResponseHandler.handleResponse(jsonAdapter, p1, transactionCallback)
            }

            override fun onFailure(p0: Call, p1: IOException) {
                println(p1.message)
            }
        })
    }
}