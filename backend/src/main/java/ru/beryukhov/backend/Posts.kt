package ru.beryukhov.backend

import com.google.gson.GsonBuilder
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.respondHtml
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*
import ru.beryukhov.common.CompletableResult
import ru.beryukhov.common.Result
import java.io.*

typealias PostModel = ru.beryukhov.common.Post

/**
 * Created by Andrey Beryukhov
 */

@KtorExperimentalLocationsAPI
fun Route.posts(backendRepository: BackendRepository) {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    post<Posts> {
        val post = call.receive<PostModel>()
        val result = backendRepository.createPost(userId = post.userId, message = post.message)
        call.respond(
            status = HttpStatusCode.OK,
            message = TextContent(
                gson.toJson(result),
                ContentType.Application.Json
            )
        )
    }

    get<Posts> {
        val posts = backendRepository.getPosts()
        call.respond(
            status = if (posts is Result.Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(posts),
                ContentType.Application.Json
            )
        )
    }

    put<Posts> {
        TODO("not implemented")
    }

    delete<Posts> {
        val post = call.receive<PostModel>()
        val result = backendRepository.deletePost(post)
        call.respond(
            status = if (result is CompletableResult.Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(result),
                ContentType.Application.Json
            )
        )
    }
}