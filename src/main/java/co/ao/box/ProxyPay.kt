/*
 *
 *  * Copyright (C) Next Solutions - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential and will be punished by law
 *  * Written by Alexandre Antonio Juca <alexandre.juca@nextbss.co.ao>
 *
 */

package co.ao.box

import co.ao.box.config.Environment
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import co.ao.box.config.ProxyPayConfig
import co.ao.box.models.EmptyBody
import co.ao.box.models.MockPaymentRequest
import co.ao.box.models.PaymentReferenceRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.lang.IllegalStateException
import java.util.logging.Logger
import kotlin.math.E

abstract class ProxyPay {
    protected val client: OkHttpClient = OkHttpClient()
    protected val moshi = Moshi.Builder().build()
    protected lateinit var config: ProxyPayConfig
    private val sandboxUrl = "https://api.sandbox.proxypay.co.ao"
    private val productionUrl = "https://api.proxypay.co.ao"
    protected lateinit var request: Request
    protected lateinit var mockPaymentRequest: MockPaymentRequest
    private lateinit var requestBody: RequestBody
    private var mockPaymentAdapter: JsonAdapter<MockPaymentRequest> = moshi.adapter(MockPaymentRequest::class.java)
    protected lateinit var referenceRequest: PaymentReferenceRequest
    private val jsonAdapter: JsonAdapter<PaymentReferenceRequest> = moshi.adapter(PaymentReferenceRequest::class.java)
    private val emptyBodyAdapter: JsonAdapter<EmptyBody> = moshi.adapter(EmptyBody::class.java)
    private val mediaType = "application/json".toMediaTypeOrNull()
    private val logger = Logger.getLogger(this.javaClass.simpleName);

    private fun buildRequest(endpoint: String, request: Request.Builder, body: RequestBody? = null) {
        when (config.getEnvironment()) {
            Environment.SANDBOX -> {
                request.url("$sandboxUrl$endpoint")
            }
            Environment.PRODUCTION -> {
                request.url("$productionUrl$endpoint")
            }
        }
        when (body != null) {
            true -> request.post(body)
        }
        request.addHeader("Accept", "application/vnd.proxypay.v2+json")
                .addHeader("Content-Type", mediaType.toString())
                .addHeader("Authorization", "Token ${config.getInstance().getApiKey()}")
        this.request = request.build()
    }

    protected fun prepareMockRequest(endpoint: String, method: String, mockPaymentRequest: MockPaymentRequest) {
        when (method) {
            "post" -> {
                if (config.getEnvironment() == Environment.SANDBOX) {
                    requestBody = RequestBody.create(mediaType, mockPaymentAdapter.toJson(mockPaymentRequest))
                    buildRequest(endpoint, Request.Builder(), requestBody)
                } else {
                    throw IllegalStateException("Can not run mock payments in production environment")
                }
            }
        }
    }

    protected fun prepareRequest(endpoint: String, method: String, request: PaymentReferenceRequest? = null,
            sendBodyInRequest: Boolean? = false) {
        when (method) {
            "get" -> {
                buildRequest(endpoint, Request.Builder().get())
            }

            "put" -> {
                requestBody = RequestBody.create(mediaType, jsonAdapter.toJson(request))
                if (sendBodyInRequest!!) {
                    buildRequest(endpoint, Request.Builder().put(requestBody))
                } else {
                    buildRequest(endpoint, Request.Builder())
                }
            }

            "delete" -> {
                buildRequest(endpoint, Request.Builder().delete())
            }

            "post" -> {
                requestBody = RequestBody.create(mediaType, jsonAdapter.toJson(request))
                if (sendBodyInRequest!!) {
                    buildRequest(endpoint, Request.Builder(), requestBody)
                } else {
                    requestBody = RequestBody.create(mediaType, emptyBodyAdapter.toJson(EmptyBody()))
                    buildRequest(endpoint, Request.Builder(), requestBody)
                }
            }
        }
    }
}