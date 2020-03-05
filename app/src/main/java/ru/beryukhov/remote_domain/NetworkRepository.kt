package ru.beryukhov.remote_domain

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.ResponseException
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor
import ru.beryukhov.common.PostApi
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Error
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.User
import ru.beryukhov.remote_domain.NetworkRepository.testError
import ru.beryukhov.remote_domain.NetworkRepository.testPosts
import ru.beryukhov.remote_domain.NetworkRepository.testUsers


private const val BASE_URL = "evening-forest-47787.herokuapp.com"
const val SOCKET_URL = "ws://$BASE_URL"
private const val SERVER_URL = "http://$BASE_URL"

/**
 * Created by Andrey Beryukhov
 */
fun testHttp(log: suspend (String) -> Unit) {
    GlobalScope.launch {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
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


object NetworkRepository : BaseHttpClient() {
    //private val SERVER_URL = "http://10.0.2.2:8080"


    suspend fun HttpClient.testPosts(log: suspend (String) -> Unit) {
        val clientPostApi = ClientPostApi(this, SERVER_URL, log)

        with(clientPostApi) {
            getPosts()
            createPost("1", "New post")
            getPosts()
            deletePost(Post("-1", "-1", "Test Post //Todo Remove"))
            getPosts()
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


}

class ClientPostApi(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val log: suspend (String) -> Unit
) : BaseHttpClient(), PostApi {
    override suspend fun createPost(userId: String, message: String): Result<Post> =
        httpClient.makeRequest<Post>("post<Result.Success<Post>>(\"$serverUrl/post\")", log) {
            post("$serverUrl/post") {
                body = Post("0", userId, message)
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

    override suspend fun getPosts(): Result<List<Post>> =
        httpClient.makeRequest<List<Post>>(
            "get<Result.Success<List<Post>>(\"$serverUrl/post\")",
            log
        ) {
            get(urlString = "$serverUrl/post")
        }

    override suspend fun updatePost(post: Post): Result<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deletePost(post: Post): CompletableResult =
        httpClient.makeCompletableRequest("delete(\"$serverUrl/post\")", log) {
            delete("$serverUrl/post") {
                body = post
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }
}

open class BaseHttpClient {
    companion object {
        val HEADER_CONTENT_TYPE = "Content-Type"
        val HEADER_JSON = "application/json"
    }

    suspend inline fun <reified T> HttpClient.makeRequest(
        logMessage: String,
        log: suspend (String) -> Unit,
        block: HttpClient.() -> Result.Success<T>
    ): Result<T> {
        log("\nSending request: $logMessage")
        try {

            val result = block.invoke(this)
            log(
                "Received result: ${result.value}"
            )
            return result

        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
            return Result.Failure(Error.HttpError(e.response.status.value))
        }
    }

    suspend inline fun HttpClient.makeCompletableRequest(
        logMessage: String,
        log: suspend (String) -> Unit,
        block: HttpClient.() -> CompletableResult
    ): CompletableResult {
        log("\nSending request: $logMessage")
        try {
            val result = block.invoke(this)
            log(
                "Received success result"
            )
            return result
        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
            return CompletableResult.Failure(Error.HttpError(e.response.status.value))
        }
    }
}