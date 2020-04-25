package ru.beryukhov.client_lib.push

import okhttp3.OkHttpClient
import okhttp3.Request

class OkHttpPush : Push {
    override fun startReceive(socketUrl: String, pushCallback: (Any) -> Unit) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("$socketUrl/ws")
            .build()
        val listener = EchoWebSocketListener(pushCallback)
        client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()
    }
}