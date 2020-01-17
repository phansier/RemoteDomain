package ru.beryukhov.backend

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

/**
 * Created by Andrey Beryukhov
 */

typealias NoSuchElementError = ru.beryukhov.common.model.Error.NoSuchElementError

@KtorExperimentalLocationsAPI
fun Route.error() {

    get<Error> {
        val error = NoSuchElementError("Test error")
        call.respond(
            status = HttpStatusCode.InternalServerError,
            message = error
        )
    }
}