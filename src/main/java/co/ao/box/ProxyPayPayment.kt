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
import co.ao.box.models.MockPaymentRequest
import co.ao.box.models.MockPaymentResponse
import co.ao.box.models.PaymentReferenceRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.joda.time.format.DateTimeFormat
import java.io.IOException

/**
 * This class is responsible for interacting wth references
 * in the ProxyPay system. The methods that this class provides
 * can create references and alter the state of existing references.
 */
class ProxyPayPayment(builder: PaymentTransactionBuilder) : ProxyPay() {
    private val jsonAdapter: JsonAdapter<String> = moshi.adapter(String::class.java)
    private val referenceAdapter: JsonAdapter<String> = moshi.adapter(String::class.java)
    private val mockPaymentAdapter: JsonAdapter<MockPaymentResponse> = moshi.adapter(MockPaymentResponse::class.java)

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

        private fun validDate(date: String): Boolean {
            val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
            return try {
                fmt.parseLocalDate(date)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        /**
         * This method is used to add a PaymentReferenceRequest that will be sent
         * to ProxyPay to create or alter a payment reference
         */
        fun addReferenceRequest(request: PaymentReferenceRequest): PaymentTransactionBuilder {
            this.request = request
            if (!validDate(request.end_datetime!!)) {
                throw IllegalArgumentException("Date is malformatted. Should be in YYYY-MM-dd format. ")
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
     * @param callback: TransactionCallback<PaymentReferenseResponse>
     * @param id: String - Valid Reference Id
     * @throws IllegalStateException - Throws exception when no reference id is provided
     */
    fun generateReference(callback: TransactionCallback<String>, id: String) {
        if (id.isBlank()) throw IllegalStateException("You must provide a valid and existing payment reference id.")
        prepareRequest("/references/$id", "put", this.referenceRequest, sendBodyInRequest = true)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(p0: Call, p1: Response) {
                handleResponse(jsonAdapter, p1, callback)
            }

            override fun onFailure(p0: Call, p1: IOException) {
                println(p1.message)
            }
        })
    }

    /**
     *  Allows the generation of a Reference Id
     * @param callback: TransactionCallback - The callback to trigger events
     */
    fun generateReferenceId(callback: TransactionCallback<String>) {
        prepareRequest("/reference_ids", "post", sendBodyInRequest = false)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(p0: Call, p1: Response) {
                handleRefResponse(referenceAdapter, p1, callback)
            }

            override fun onFailure(p0: Call, p1: IOException) {
                println(p1.message)
            }
        })
    }

    fun mockPayment(callback: TransactionCallback<MockPaymentResponse>) {
        prepareMockRequest("/payments", "post", this.mockPaymentRequest)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(p0: Call, p1: Response) {
                handleMockPaymentResponse(mockPaymentAdapter, p1, callback)
            }

            override fun onFailure(p0: Call, p1: IOException) {
                println(p1.message)
            }
        })
    }

    private fun handleRefResponse(
            adapter: JsonAdapter<String>,
            response: Response,
            callback: TransactionCallback<String>
    ) {
        when (response.code) {
            200 -> {
                callback.onSuccess(response.body?.string()!!)
            }
            401 -> {
                callback.onFailure("Your API key is wrong")
            }
            400 -> {
                callback.onFailure("Bad Request -- Request is malformed.")
            }
            404 -> {
                callback.onFailure("Not Found -- The specified resource could not be found")
            }
            405 -> {
                callback.onFailure("Method Now Allowed -- Tou tried to accesss a resource with an invalid HTTP method")
            }
            406 -> {
                callback.onFailure("Not Acceptable -- You requested a format that is not json")
            }
            422 -> {
                callback.onFailure("Unprocessable Entity -- Your request includes invalid fields. Check the response body for details")
            }
            429 -> {
                callback.onFailure("Too Many Requests -- You're exceeding the API rate limit! Reduce the number of requests / minute.")
            }
            500 -> {
                callback.onFailure("Internal Server Error -- We had a problem with our server. Try again later ")
            }
            503 -> {
                callback.onFailure("Service Unavailable -- We're temporarily offline for maintenance. Please try again later.")
            }
            else -> {
                callback.onFailure("An error occurred while attempting to generate a new payment reference => HTTP Status ${response.code}")
            }
        }
    }

    private fun handleDeleteResponse(
            adapter: JsonAdapter<String>,
            response: Response,
            callback: TransactionCallback<String>
    ) {
        when (response.code) {
            200 -> {
                callback.onSuccess(adapter.fromJson(response.body?.string()!!)!!)
            }
            204 -> {
                callback.onSuccess("Success")
            }
            401 -> {
                callback.onFailure("Your API key is wrong")
            }
            400 -> {
                callback.onFailure("Bad Request -- Request is malformed.")
            }
            404 -> {
                callback.onFailure("Not Found -- The specified resource could not be found")
            }
            405 -> {
                callback.onFailure("Method Now Allowed -- Tou tried to accesss a resource with an invalid HTTP method")
            }
            406 -> {
                callback.onFailure("Not Acceptable -- You requested a format that is not json")
            }
            422 -> {
                callback.onFailure("Unprocessable Entity -- Your request includes invalid fields. Check the response body for details")
            }
            429 -> {
                callback.onFailure("Too Many Requests -- You're exceeding the API rate limit! Reduce the number of requests / minute.")
            }
            500 -> {
                callback.onFailure("Internal Server Error -- We had a problem with our server. Try again later ")
            }
            503 -> {
                callback.onFailure("Service Unavailable -- We're temporarily offline for maintenance. Please try again later.")
            }
            else -> {
                callback.onFailure("An error occurred => HTTP Status ${response.code}")
            }
        }
    }

    private fun handleResponse(
            adapter: JsonAdapter<String>,
            response: Response,
            callback: TransactionCallback<String>
    ) {
        when (response.code) {
            200 -> {
                callback.onSuccess(adapter.fromJson(response.body?.string()!!)!!)
            }
            204 -> {
                callback.onSuccess("Reference was created or updated successfully")
            }
            401 -> {
                callback.onFailure("Your API key is wrong")
            }
            400 -> {
                callback.onFailure("Bad Request -- Request is malformed.")
            }
            404 -> {
                callback.onFailure("Not Found -- The specified resource could not be found")
            }
            405 -> {
                callback.onFailure("Method Now Allowed -- Tou tried to accesss a resource with an invalid HTTP method")
            }
            406 -> {
                callback.onFailure("Not Acceptable -- You requested a format that is not json")
            }
            422 -> {
                callback.onFailure("Unprocessable Entity -- Your request includes invalid fields. Check the response body for details")
            }
            429 -> {
                callback.onFailure("Too Many Requests -- You're exceeding the API rate limit! Reduce the number of requests / minute.")
            }
            500 -> {
                callback.onFailure("Internal Server Error -- We had a problem with our server. Try again later ")
            }
            503 -> {
                callback.onFailure("Service Unavailable -- We're temporarily offline for maintenance. Please try again later.")
            }
            else -> {
                callback.onFailure("An error occurred => HTTP Status ${response.code}")
            }
        }
    }

    private fun handleMockPaymentResponse(
            adapter: JsonAdapter<MockPaymentResponse>,
            response: Response,
            callback: TransactionCallback<MockPaymentResponse>
    ) {
        when (response.code) {
            200 -> {
                callback.onSuccess(adapter.fromJson(response.body?.string()!!)!!)
            }
            401 -> {
                callback.onFailure("Your API key is wrong")
            }
            400 -> {
                callback.onFailure("Bad Request -- Request is malformed.")
            }
            404 -> {
                callback.onFailure("Not Found -- The specified resource could not be found")
            }
            405 -> {
                callback.onFailure("Method Now Allowed -- Tou tried to accesss a resource with an invalid HTTP method")
            }
            406 -> {
                callback.onFailure("Not Acceptable -- You requested a format that is not json")
            }
            422 -> {
                callback.onFailure("Unprocessable Entity -- Your request includes invalid fields. Check the response body for details")
            }
            429 -> {
                callback.onFailure("Too Many Requests -- You're exceeding the API rate limit! Reduce the number of requests / minute.")
            }
            500 -> {
                callback.onFailure("Internal Server Error -- We had a problem with our server. Try again later ")
            }
            503 -> {
                callback.onFailure("Service Unavailable -- We're temporarily offline for maintenance. Please try again later.")
            }
            else -> {
                callback.onFailure("An error occurred while attempting to generate a new payment reference => HTTP Status ${response.code}")
            }
        }
    }

    /**
     * Deletes a reference with a given Id
     * @param callback: TransactionCallback<String>
     * @param id: String - Valid Reference Id
     * @throws IllegalStateException - Throws exception when no reference id is provided
     */
    fun deleteReference(transactionCallback: TransactionCallback<String>, id: String) {
        if (id.isBlank()) throw IllegalStateException("You must provide a valid and existing payment reference id.")
        prepareRequest("/references/$id", "delete", this.referenceRequest, sendBodyInRequest = false)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(p0: Call, p1: Response) {
                handleDeleteResponse(jsonAdapter, p1, transactionCallback)
            }

            override fun onFailure(p0: Call, p1: IOException) {
                println(p1.message)
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
        if (id.isBlank()) throw IllegalStateException("You must provide a valid and existing payment reference id.")
        prepareRequest("/payments/$id", "delete", this.referenceRequest, sendBodyInRequest = false)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(p0: Call, p1: Response) {
                handleDeleteResponse(jsonAdapter, p1, transactionCallback)
            }

            override fun onFailure(p0: Call, p1: IOException) {
                println(p1.message)
            }
        })
    }
}