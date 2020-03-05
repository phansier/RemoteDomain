package ru.beryukhov.remote_domain.http

import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Error
import ru.beryukhov.common.model.Result

open class BaseHttpClient {
    companion object {
        val HEADER_CONTENT_TYPE = "Content-Type"
        val HEADER_JSON = "application/json"
    }

    suspend inline fun <reified T> HttpClient.makeRequest(
        logMessage: String,
        log: suspend (String) -> Unit,
        block: HttpClient.() -> Result.Success<T>
    ): Result<T> {
        log("\nSending request: $logMessage")
        try {

            val result = block.invoke(this)
            log(
                "Received result: ${result.value}"
            )
            return result

        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
            return Result.Failure(
                Error.HttpError(
                    e.response.status.value
                )
            )
        }
    }

    suspend inline fun HttpClient.makeCompletableRequest(
        logMessage: String,
        log: suspend (String) -> Unit,
        block: HttpClient.() -> CompletableResult
    ): CompletableResult {
        log("\nSending request: $logMessage")
        try {
            val result = block.invoke(this)
            log(
                "Received success result"
            )
            return result
        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
            return CompletableResult.Failure(
                Error.HttpError(e.response.status.value)
            )
        }
    }
}