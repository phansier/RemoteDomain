package ru.beryukhov.backend.routes

import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.beryukhov.backend.BackendRepository
import ru.beryukhov.common.model.Success
import java.util.*


@KtorExperimentalLocationsAPI
@Location("/clientid")
class ClientId

private const val PASSWORD_ENCODED = "passwordEncoded"

@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Route.clientId(
    backendRepository: BackendRepository,
    gson: Gson,
    usersTable: MutableMap<String, ByteArray>
) {
    get<ClientId> {
        val clientIdResult = backendRepository.getClientId()
        val passwordEncoded = call.request.queryParameters[PASSWORD_ENCODED]
        if (passwordEncoded!=null) {
            usersTable[(clientIdResult as Success).value] = Base64.getDecoder().decode(passwordEncoded)
            call.respond(
                message = TextContent(
                    gson.toJson(clientIdResult),
                    ContentType.Application.Json
                )
            )
        }
        else{
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = TextContent("PASSWORD_ENCODED param missed", contentType = ContentType.Text.Plain)
            )
        }
    }
}