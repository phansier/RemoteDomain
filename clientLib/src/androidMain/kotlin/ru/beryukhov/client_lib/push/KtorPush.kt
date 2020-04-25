package ru.beryukhov.client_lib.push

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
actual fun createWebSocketHttpClient(): HttpClient {
    return HttpClient(OkHttp.create()) {
        install(WebSockets) {

        }
    }
}

actual fun DefaultClientWebSocketSession.configure(
    timeoutMillis: Long,
    pingMillis: Long
) {
    this.timeoutMillis = timeoutMillis
    pingIntervalMillis = pingMillis
}