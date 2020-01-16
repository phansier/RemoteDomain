package ru.beryukhov.backend

import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.Result


/**
 * Created by Andrey Beryukhov
 */

@KtorExperimentalLocationsAPI
fun Route.posts(backendRepository: BackendRepository) {
//    val gson = GsonBuilder()
//        .setPrettyPrinting()
//        .create()

    post<Posts> {
        val post = call.receive<Post>()
        val result = backendRepository.createPost(userId = post.userId, message = post.message)
        call.respond(
            status = HttpStatusCode.OK,
            message = result
        )
    }

    get<Posts> {
        val posts = backendRepository.getPosts()
        call.respond(
            status = if (posts is Result.Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = posts
        )
    }

    put<Posts> {
        TODO("not implemented")
    }

    delete<Posts> {
        val post = call.receive<Post>()
        val result = backendRepository.deletePost(post)
        call.respond(
            status = if (result is CompletableResult.Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = result
        )
    }
}