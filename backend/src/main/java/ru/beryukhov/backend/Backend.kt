package ru.beryukhov.backend

import com.google.gson.GsonBuilder
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserHashedTableAuth
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getDigestFunction
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import ru.beryukhov.backend.routes.clientId
import ru.beryukhov.backend.routes.entities
import ru.beryukhov.backend.routes.error
import ru.beryukhov.backend.routes.styles
import ru.beryukhov.common.ApiRequest
import java.util.*

//https://github.com/ktorio/ktor-samples/tree/master/app/youkube
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Application.main() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)
    // Allows to use classes annotated with @Location to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    install(Locations)
    // Automatic '304 Not Modified' Responses
    install(ConditionalHeaders)
    // Supports for Range, Accept-Range and Content-Range headers
    install(PartialContent)

    install(ContentNegotiation) {
        //register(ContentType.Application.Json, SerializationConverter())
        register(
            ContentType.Application.Json,
            GsonConverter(GsonBuilder().serializeNulls().create())
        )
    }

    val channel = BroadcastChannel<ApiRequest>(Channel.CONFLATED)

    val backendRepository = BackendRepository(
        entityRepository = EntityRepository(broadcastChannel = channel)
    )
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    install(WebSockets)
    install(Routing) {
        launchWebSocket(channel)
    }
    install(Authentication) {
        basic("hashed") {
            realm = "ktor"
            validate { hashedUserTable.authenticate(it) }
        }
    }

    // Register all the routes available to this application.
    // To allow better scaling for large applications,
    // we have moved those route registrations into several extension methods and files.
    routing {
        authenticate("hashed") {
            entities(backendRepository, gson)
        }
        clientId(backendRepository, gson, usersTable)

        error()

        styles()
        static {
            // This marks index.html from the 'web' folder in resources as the default file to serve.
            defaultResource("index.html", "web")
            // This serves files from the 'web' folder in the application resources.
            resources("web")
        }


    }
}

val usersTable = mutableMapOf(
    //todo remove test account
    "test" to Base64.getDecoder().decode("GSjkHCHGAxTTbnkEDBbVYd+PUFRlcWiumc4+MWE9Rvw=") // sha256 for "test"
)

@KtorExperimentalAPI
val hashedUserTable = UserHashedTableAuth(
    getDigestFunction("SHA-256") { "ktor${it.length}" },
    table = usersTable
)


@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
private fun Routing.launchWebSocket(channel: BroadcastChannel<ApiRequest>) {

    webSocket("/ws") {
        GlobalScope.launch {
            channel.consumeEach {
                println("event got $it")
                println("event got json ${it.json}")
                outgoing.send(Frame.Text(it.json))
            }
        }

        while (true) {
            val value = incoming.receiveOrClosed()
            println("incoming $value from $this")
            if (value.isClosed) {
                break
            }
        }

    }
}

