package ru.beryukhov.backend

import io.ktor.http.HttpStatusCode
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Result

/**
 * Created by Andrey Beryukhov
 */

fun Result<out Any>.toResponse(): Response {
    return when (this) {
        is Result.Success -> Response(HttpStatusCode.OK, value)
        is Result.Failure -> Response(HttpStatusCode.InternalServerError, error)
    }
}

fun CompletableResult.toResponse(): Response {
    return when (this) {
        is CompletableResult.Success -> Response(HttpStatusCode.OK, CompletableResult.Success)
        is CompletableResult.Failure -> Response(HttpStatusCode.InternalServerError, error)
    }
}

data class Response(val status: HttpStatusCode, val message: Any)
