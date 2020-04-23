package ru.beryukhov.client_lib.push

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class OkHttpPush : Push {
    override fun startReceive(socketUrl: String, log: suspend (String) -> Unit, pushCallback: (Any) -> Unit) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("$socketUrl/ws").build()
        val listener =
            EchoWebSocketListener(
                log,
                pushCallback
            )
        val ws: WebSocket = client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()
    }
}