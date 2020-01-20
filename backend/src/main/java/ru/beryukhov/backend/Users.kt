package ru.beryukhov.backend

import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.User

/**
 * Created by Andrey Beryukhov
 */
@KtorExperimentalLocationsAPI
fun Route.users(backendRepository: BackendRepository) {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    post<Users> {
        val user = call.receive<User>()
        val result = backendRepository.createUser(userName = user.userName)
        call.respond(
            status = HttpStatusCode.OK,
            message = TextContent(
                gson.toJson(result),
                ContentType.Application.Json
            )
        )
    }

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

    put<Users> {
        TODO("not implemented")
    }

    delete<Users> {
        val user = call.receive<User>()
        val result = backendRepository.deleteUser(user)
        call.respond(
            status = if (result is CompletableResult.Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(result),
                ContentType.Application.Json
            )
        )
    }
}