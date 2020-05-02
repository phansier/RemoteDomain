package ru.beryukhov.backend.routes

import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.beryukhov.backend.BackendRepository


@KtorExperimentalLocationsAPI
@Location("/clientid")
class ClientId


@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Route.clientId(
    backendRepository: BackendRepository,
    gson: Gson
) {
    get<ClientId> {
        val clientIdResult = backendRepository.getClientId()
        call.respond(
            message = TextContent(
                gson.toJson(clientIdResult),
                ContentType.Application.Json
            )
        )
    }
}