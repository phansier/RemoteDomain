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
import ru.beryukhov.common.Result

/**
 * Created by Andrey Beryukhov
 */
@KtorExperimentalLocationsAPI
fun Route.users(backendRepository: BackendRepository) {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    get<Users> {
        val users = backendRepository.getUsers()
        call.respond(
            status = if (users is Result.Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(users),
                ContentType.Application.Json
            )
        )
    }
}