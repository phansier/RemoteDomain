package ru.beryukhov.backend

import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.TextContent
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.beryukhov.common.model.Error


/**
 * Created by Andrey Beryukhov
 */
/*
@KtorExperimentalLocationsAPI
fun Route.postsDiff(backendRepository: BackendRepository) {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()


    get<PostsDiff> {
        val queryParameters: Parameters = call.request.queryParameters
        val from: Long? = queryParameters["from"]?.toLongOrNull()
        val to: Long? = queryParameters["to"]?.toLongOrNull()
        if (from == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = TextContent(
                    gson.toJson(Error.InvalidParam("from")),
                    ContentType.Application.Json
                )
            )
            return@get
        }
        if (to == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = TextContent(
                    gson.toJson(Error.InvalidParam("to")),
                    ContentType.Application.Json
                )
            )
            return@get
        }

        */
/*val posts = backendRepository.getPostsDiff(from, to)
        call.respond(
            status = if (posts is Result.Success) HttpStatusCode.OK else HttpStatusCode.InternalServerError,//todo make mapping for exceptions
            message = TextContent(
                gson.toJson(posts),
                ContentType.Application.Json
            )
        )*//*


    }
}*/
