/*
 *
 *  * Copyright (C) Next Solutions - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential and will be punished by law
 *  * Written by Alexandre Antonio Juca <alexandre.juca@nextbss.co.ao>
 *
 */

package core

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import core.client.TransactionCallback
import core.config.ProxyPayConfig
import core.models.ReferencesResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * This class is responsible for interacting wth references
 * in the ProxyPay system. The methods that this class provides
 * will only return references but never create or change
 * them.
 */
class ProxyPayReference(builder: ProxyReferenceBuilder) : ProxyPay() {
    private val type = Types.newParameterizedType(List::class.java, ReferencesResponse::class.java)
    private val jsonAdapter: JsonAdapter<List<ReferencesResponse>> = moshi.adapter(type)

    init {
        super.config = builder.config
    }

    class ProxyReferenceBuilder {
        lateinit var config: ProxyPayConfig
        fun addProxyPayConfiguration(config: ProxyPayConfig): ProxyReferenceBuilder {
            this.config = config
            return this
        }

        fun build(): ProxyPayReference {
            return ProxyPayReference(this)
        }
    }

    /**
     * Returns any Payment events stored on the server that were not yet Acknowledged by the client application.
     * Specify the amount of references (between 1 and 100), defaults to 100
     * @param callback: TransactionCallback - The callback to trigger events
     * @param itemsToReturn: Int - The amount of references to return (defaults to 100)
     * @throws IllegalStateException: Exception - When the value specified by itemsToReturn is not in the range of 1 to 100
     */
    fun getPayments(callback: TransactionCallback<List<ReferencesResponse>>, itemsToReturn: Int = 100) {
        if (itemsToReturn !in 1..100) {
            throw IllegalStateException("You must specify a value between 1 and 100")
        }
        prepareRequest("/payments?n=$itemsToReturn", "get")
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(p0: Call, p1: Response) {
                handleResponse(jsonAdapter, p1, callback)
            }

            override fun onFailure(p0: Call, p1: IOException) {
                println(p1.message)
            }
        })
    }

    private fun handleResponse(
        adapter: JsonAdapter<List<ReferencesResponse>>,
        response: Response,
        callback: TransactionCallback<List<ReferencesResponse>>
    ) {
        when (response.code()) {
            200 -> {
                callback.onSuccess(adapter.fromJson(response.body()?.string()!!)!!)
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
                callback.onFailure("An error occured while attempting to generate a new payment reference => HTTP Status ${response.code()}")
            }
        }
    }
}