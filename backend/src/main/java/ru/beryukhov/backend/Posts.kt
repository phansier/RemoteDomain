package ru.beryukhov.backend

import com.google.gson.GsonBuilder
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.respondHtml
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*
import ru.beryukhov.common.Result
import java.io.*


/**
 * Created by Andrey Beryukhov
 */

@KtorExperimentalLocationsAPI
fun Route.posts(backendRepository: BackendRepository) {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

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



    get<Post> {
        //val post = database.
    }
}