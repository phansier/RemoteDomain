package ru.beryukhov.backend

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
import java.io.*


/**
 * Created by Andrey Beryukhov
 */

@KtorExperimentalLocationsAPI
fun Route.videos(di: String) {
    get<Index> {
        call.respondHtml {
            body {
                div("posts") {
                    p { text("Posts") }
                }
            }
        }
    }
    get<Post> {

    }
}