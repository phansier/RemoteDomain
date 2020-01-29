
package ru.beryukhov.backend

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.write
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import java.net.InetSocketAddress

@KtorExperimentalLocationsAPI
@Location("/")
class Index

@KtorExperimentalLocationsAPI
@Location("/post")
class Posts

@KtorExperimentalLocationsAPI
@Location("/diff/post")
class PostsDiff

@KtorExperimentalLocationsAPI
@Location("/post/{id}")
data class Post(val id: String)

@KtorExperimentalLocationsAPI
@Location("/user")
class Users

@KtorExperimentalLocationsAPI
@Location("/error")
class Error

//https://github.com/ktorio/ktor-samples/tree/master/app/youkube
@FlowPreview
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
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
        register(ContentType.Application.Json, GsonConverter())
    }

    val channel = BroadcastChannel<Any>(1)

    val backendRepository = BackendRepository(
        postRepository = PostRepository(broadcastChannel = channel),
        userRepository = UserRepository(broadcastChannel = channel)

    )

    launchSocket(channel.openSubscription())

    // Register all the routes available to this application.
    // To allow better scaling for large applications,
    // we have moved those route registrations into several extension methods and files.
    routing {
        posts(backendRepository)
        postsDiff(backendRepository)
        users(backendRepository)

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

@UseExperimental(FlowPreview::class)
@KtorExperimentalAPI
private fun launchSocket(openSubscription: ReceiveChannel<Any>) {
    GlobalScope.launch {
        //telnet 127.0.0.1 2324
        val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
            .bind(InetSocketAddress("127.0.0.1", 2324))
        println("Started echo telnet server at ${server.localAddress}")

        while (true) {
            val socket = server.accept()

            launch {
                println("Socket accepted: ${socket.remoteAddress}")

                /*val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)

                try {
                    while (true) {
                        val line = input.readUTF8Line()

                        println("${socket.remoteAddress}: $line")
                        output.write("$line received")
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    socket.close()
                }*/
                val output = socket.openWriteChannel(autoFlush = true)
                openSubscription.consumeAsFlow().onEach {
                    output.write("Data changed")
                }
            }
        }
    }
}