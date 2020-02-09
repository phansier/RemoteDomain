package ru.beryukhov.remote_domain

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.ResponseException
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.User
import ru.beryukhov.remote_domain.NetworkRepository.testError
import ru.beryukhov.remote_domain.NetworkRepository.testPosts
import ru.beryukhov.remote_domain.NetworkRepository.testUsers


private const val BASE_URL = "evening-forest-47787.herokuapp.com"
private const val SOCKET_URL = "ws://$BASE_URL"
private const val SERVER_URL = "http://$BASE_URL"

/**
 * Created by Andrey Beryukhov
 */
fun testHttp(log: suspend (String) -> Unit) {
    GlobalScope.launch {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        val client = HttpClient(OkHttp) {
            install(JsonFeature) {
                serializer = GsonSerializer {
                    serializeNulls()
                    disableHtmlEscaping()
                }
            }
            engine {
                addInterceptor(interceptor)
            }

        }

        client.testPosts(log)
        client.testUsers(log)
        client.testError(log)

        client.close()
    }
}

fun testSocket(log: suspend (String) -> Unit){
    val client = OkHttpClient()
    val request: Request = Request.Builder().url("$SOCKET_URL/ws").build()
    val listener = EchoWebSocketListener(log)
    val ws: WebSocket = client.newWebSocket(request, listener)
    client.dispatcher.executorService.shutdown()
}

private class EchoWebSocketListener(private val log: suspend (String) -> Unit) : WebSocketListener() {
    fun output(s: String){
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                log(s)
            }
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        /*webSocket.send("Hello, it's SSaurel !")
        webSocket.send("What's up ?")
        webSocket.send("deadbeef".decodeHex())
        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !")*/
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        output("Receiving : $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        output("Receiving bytes : " + bytes.hex())
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
        output("Error : " + t.message)
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}

@KtorExperimentalAPI
fun testSocket1(log: suspend (String) -> Unit){
    //main thread
    CoroutineScope(Dispatchers.IO).launch {
        log("Test Socket: ")
        try {
            val client = HttpClient(OkHttp.create()) {
                install(WebSockets) {

                }
            }

            client.ws(
                method = HttpMethod.Get,
                host = SOCKET_URL,
                //port = 8080,
                path = "/ws"
            ) {
                send(Frame.Text("Hello World"))
                timeoutMillis = 60_000 * 15
                pingIntervalMillis = 1_000
                while (true) {
                    // Receive frame.
                    val frame = incoming.receive()
                    log("WS received")
                    when (frame) {
                        is Frame.Text -> log("WS received Text: ${frame.readText()}")
                        is Frame.Binary -> log("WS received Binary: ${frame.readBytes()}")
                    }
                }
            }

            client.close()
        }
        catch (e: Throwable){
            log("WS error: ${e.message}")
        }
    }
}



object NetworkRepository {
    //private val SERVER_URL = "http://10.0.2.2:8080"
    private const val HEADER_CONTENT_TYPE = "Content-Type"
    private const val HEADER_JSON = "application/json"


    suspend fun HttpClient.testPosts(log: suspend (String) -> Unit) {
        makeRequest("get<Result.Success<List<Post>>(\"$SERVER_URL/post\")", log) {
            get<Result.Success<List<Post>>>("$SERVER_URL/post")
        }

        makeRequest("post<Result.Success<Post>>(\"$SERVER_URL/post\")", log) {
            post<Result.Success<Post>>("$SERVER_URL/post") {
                body = Post("0", "1", "New post")
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

        makeRequest("get<Result.Success<List<Post>>(\"$SERVER_URL/post\")", log) {
            get<Result.Success<List<Post>>>("$SERVER_URL/post")
        }

        makeCompletableRequest("delete(\"$SERVER_URL/post\")", log) {
            delete("$SERVER_URL/post") {
                body = Post("-1", "-1", "Test Post //Todo Remove")
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

        makeRequest("get<Result.Success<List<Post>>(\"$SERVER_URL/post\")", log) {
            get<Result.Success<List<Post>>>("$SERVER_URL/post")
        }
    }

    suspend fun HttpClient.testUsers(log: suspend (String) -> Unit) {
        makeRequest("get<Result.Success<List<User>>(\"$SERVER_URL/user\")", log) {
            get<Result.Success<List<User>>>("$SERVER_URL/user")
        }

        makeRequest("post<Result.Success<User>>(\"$SERVER_URL/user\")", log) {
            post<Result.Success<User>>("$SERVER_URL/user") {
                body = User("0", "New user")
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

        makeRequest("get<Result.Success<List<User>>(\"$SERVER_URL/user\")", log) {
            get<Result.Success<List<User>>>("$SERVER_URL/user")
        }

        makeCompletableRequest("delete(\"$SERVER_URL/user\")", log) {
            delete("$SERVER_URL/user") {
                body = User("-1", "Test Testov //Todo Remove")
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

        makeRequest("get<Result.Success<List<User>>(\"$SERVER_URL/user\")", log) {
            get<Result.Success<List<User>>>("$SERVER_URL/user")
        }
    }

    suspend fun HttpClient.testError(log: suspend (String) -> Unit) {
        makeRequest("get<Result.Success<List<User>>>(\"$SERVER_URL/error\")", log) {
            get<Result.Success<List<User>>>("$SERVER_URL/error")
        }
    }

    suspend inline fun <reified T> HttpClient.makeRequest(
        logMessage: String,
        log: suspend (String) -> Unit,
        block: HttpClient.() -> Result.Success<T>
    ) {
        log("\nSending request: $logMessage")
        try {

            val result = block.invoke(this)
            log(
                "Received result: ${result.value}"
            )
        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
        }
    }

    suspend inline fun HttpClient.makeCompletableRequest(
        logMessage: String,
        log: suspend (String) -> Unit,
        block: HttpClient.() -> CompletableResult.Success
    ) {
        log("\nSending request: $logMessage")
        try {

            val result = block.invoke(this)
            log(
                "Received success result"
            )
        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
        }
    }
}