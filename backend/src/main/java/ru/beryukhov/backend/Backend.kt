
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
import io.ktor.routing.routing

@KtorExperimentalLocationsAPI
@Location("/")
class Index

@KtorExperimentalLocationsAPI
@Location("/post")
class Posts

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

    val backendRepository = BackendRepository(
        postRepository = PostRepository(),
        userRepository = UserRepository()
    )

    // Register all the routes available to this application.
    // To allow better scaling for large applications,
    // we have moved those route registrations into several extension methods and files.
    routing {
        posts(backendRepository)
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