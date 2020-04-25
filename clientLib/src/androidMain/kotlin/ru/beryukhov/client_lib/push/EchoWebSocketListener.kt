package ru.beryukhov.client_lib.push

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import ru.beryukhov.client_lib.log

class EchoWebSocketListener(
    private val pushCallback: (Any) -> Unit
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        log("EchoWebSocketListener", "Open : $response")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        log("EchoWebSocketListener", "Receiving : $text")
        pushCallback(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        log("EchoWebSocketListener", "Receiving bytes : ${bytes.hex()}")
    }

    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String
    ) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        log("EchoWebSocketListener", "Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        log("EchoWebSocketListener", "Error : $t + message: ${t.message}")
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}