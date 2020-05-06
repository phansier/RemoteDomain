package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import ru.beryukhov.client_lib.log
import ru.beryukhov.common.model.*

open class BaseHttpClient {
    companion object {
        const val HEADER_JSON = "application/json"
    }

    inline fun <T> HttpClient.makeRequest(
        logMessage: String,
        block: HttpClient.() -> Success<T>
    ): Result<T> {
        log("BaseHttpClient", "\nSending request: $logMessage")
        try {
            val result = block.invoke(this)
            log("BaseHttpClient", "Received result: ${result.value}")
            return result

        } catch (e: ResponseException) {
            log("BaseHttpClient", "Received error response: status = ${e.response.status}")
            return Failure(
                HttpError(
                    e.response.status.value
                )
            )
        }
    }

    inline fun HttpClient.makeCompletableRequest(
        logMessage: String,
        block: HttpClient.() -> CompletableResult
    ): CompletableResult {
        log("BaseHttpClient", "\nSending request: $logMessage")
        try {
            val result = block.invoke(this)
            log("BaseHttpClient", "Received success result")
            return result
        } catch (e: ResponseException) {
            log("BaseHttpClient", "Received error response: status = ${e.response.status}")
            return CompletableFailure(
                HttpError(e.response.status.value)
            )
        }
    }
}