package ru.beryukhov.backend.routes

import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.beryukhov.backend.BackendRepository
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.CompletableSuccess
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Success


/**
 * Created by Andrey Beryukhov
 */

@KtorExperimentalLocationsAPI
@Location("/entity")
class Entities

@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Route.entities(
    backendRepository: BackendRepository
) {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    post<Entities> {
        val entity = call.receive<Entity>()
        val result = backendRepository.create(entity)
        call.respond(
            status = if (result is Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(result),
                ContentType.Application.Json
            )
        )
    }

    get<Entities> {
        val posts = backendRepository.get()
        call.respond(
            status = if (posts is Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(posts),
                ContentType.Application.Json
            )
        )
    }

    put<Entities> {
        TODO("not implemented")
    }

    delete<Entities> {
        val entity = call.receive<Entity>()
        val result = backendRepository.delete(entity)
        call.respond(
            status = if (result is CompletableSuccess) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(result),
                ContentType.Application.Json
            )
        )
    }
}