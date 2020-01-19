package ru.beryukhov.backend

import io.ktor.application.call
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.beryukhov.common.model.Post


/**
 * Created by Andrey Beryukhov
 */

@KtorExperimentalLocationsAPI
fun Route.posts(backendRepository: BackendRepository) {

    post<Posts> {
        val post = call.receive<Post>()
        val result = backendRepository.createPost(userId = post.userId, message = post.message)
        val response = result.toResponse()
        call.respond(response.status, response.message)
    }

    get<Posts> {
        val posts = backendRepository.getPosts()
        val response = posts.toResponse()
        call.respond(response.status, response.message)
    }

    put<Posts> {
        TODO("not implemented")
    }

    delete<Posts> {
        val post = call.receive<Post>()
        val result = backendRepository.deletePost(post)
        val response = result.toResponse()
        call.respond(response.status, response.message)
    }
}