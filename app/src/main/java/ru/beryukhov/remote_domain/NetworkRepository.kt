package ru.beryukhov.remote_domain

import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import ru.beryukhov.common.CompletableResult
import ru.beryukhov.common.Post
import ru.beryukhov.common.Result
import ru.beryukhov.common.User

/**
 * Created by Andrey Beryukhov
 */
object NetworkRepository {
    private val SERVER_URL = "http://10.0.2.2:8080"
    private val HEADER_CONTENT_TYPE = "Content-Type"
    private val HEADER_JSON = "application/json"

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