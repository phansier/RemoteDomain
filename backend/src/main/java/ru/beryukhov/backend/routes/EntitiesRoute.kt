package ru.beryukhov.backend.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.principal
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.beryukhov.backend.BackendRepository
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
    backendRepository: BackendRepository,
    gson: Gson
) {

    post<Entities> {
        val entity = call.receive<Entity>()
        val clientId = call.principal<UserIdPrincipal>()?.name
        if (clientId == null){
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = TextContent("ClientId is null", contentType = ContentType.Text.Plain)
            )
            return@post
        }
        val result = backendRepository.post(entity, clientId)
        call.respond(
            status = if (result is Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(result),
                ContentType.Application.Json
            )
        )
    }

    get<Entities> {
        val clientId = call.principal<UserIdPrincipal>()?.name
        if (clientId == null){
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = TextContent("ClientId is null", contentType = ContentType.Text.Plain)
            )
            return@get
        }
        val entity = backendRepository.get(clientId)
        call.respond(
            status = if (entity is Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(entity),
                ContentType.Application.Json
            )
        )
    }
}
