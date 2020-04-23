package ru.beryukhov.client_lib.push

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class EchoWebSocketListener(
    private val log: suspend (String) -> Unit,
    private val pushCallback: (Any) -> Unit
) : WebSocketListener() {
    fun output(s: String) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                log("EchoWebSocketListener $s")
            }
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        output("Open : $response")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        output("Receiving : $text")
        pushCallback(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        output("Receiving bytes : ${bytes.hex()}")
    }

    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String
    ) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        output("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        output("Error : $t + message: ${t.message}")
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}