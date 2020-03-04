package ru.beryukhov.remote_domain.push

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import ru.beryukhov.remote_domain.push.Push

/**
 * Not working yet
 * Use [OkHttpPush]
 */
@InternalCoroutinesApi
@KtorExperimentalAPI
class KtorPush : Push {
    override fun startReceive(socketUrl: String, log: suspend (String) -> Unit, pushCallback: (Any) -> Unit) {
        //main thread
        CoroutineScope(Dispatchers.IO)
            .launch {
            log("Test Socket: ")
            try {
                val client =
                    HttpClient(OkHttp.create()) {
                        install(WebSockets) {

                        }
                    }

                client.ws(
                    method = HttpMethod.Get,
                    host = socketUrl,
                    //port = 8080,
                    path = "/ws"
                ) {
                    //send(Frame.Text("Hello World"))
                    timeoutMillis = 60_000 * 15
                    pingIntervalMillis = 1_000

                    while (true) {
                        val value = incoming.receiveOrClosed()
                        println("incoming $value from $this")
                        if (value.isClosed) {
                            break
                        }
                        pushCallback(value)
                    }
                    /*while (true) {
                        // Receive frame.
                        val frame = incoming.receive()
                        log("WS received")
                        when (frame) {
                            is Frame.Text -> log("WS received Text: ${frame.readText()}")
                            is Frame.Binary -> log("WS received Binary: ${frame.readBytes()}")
                        }
                    }*/
                }

                client.close()
            } catch (e: Throwable) {
                log("WS error: ${e.message}")
            }
        }
    }
}