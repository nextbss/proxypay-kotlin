package co.ao.box.core

import co.ao.box.client.TransactionCallback
import co.ao.box.models.MockPaymentResponse
import com.squareup.moshi.JsonAdapter
import okhttp3.Response

object ApiResponseHandler {

    fun handleMockPaymentResponse(adapter: JsonAdapter<MockPaymentResponse>, response: Response,
                                  callback: TransactionCallback<MockPaymentResponse>) {
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

    fun handleResponse(adapter: JsonAdapter<String>, response: Response, callback: TransactionCallback<String>) {
        when (response.code) {
            200 -> {
                callback.onSuccess(adapter.fromJson(response.body?.string()!!)!!)
            }
            204 -> {
                callback.onSuccess("Operation terminated successfully")
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
}