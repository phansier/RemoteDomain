package ru.beryukhov.backend

import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Failure

/**
 * Created by Andrey Beryukhov
 */

typealias NoSuchElementError = ru.beryukhov.common.model.Error.NoSuchElementError

@KtorExperimentalLocationsAPI
fun Route.error() {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    get<Error> {
        val users = Failure<Entity>(NoSuchElementError(""))
        call.respond(
            status = HttpStatusCode.InternalServerError,
            message = TextContent(
                gson.toJson(users),
                ContentType.Application.Json
            )
        )
    }
}