package ru.beryukhov.remote_domain.http

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import ru.beryukhov.client_lib.http.BaseHttpClient
import ru.beryukhov.common.PostApi
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.Result

class ClientPostApi(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val log: suspend (String) -> Unit
) : BaseHttpClient(),
    PostApi {
    override suspend fun createPost(userId: String, message: String): Result<Post> =
        httpClient.makeRequest<Post>("post<Result.Success<Post>>(\"$serverUrl/post\")", log) {
            post("$serverUrl/post") {
                body = Post("0", userId, message)
                headers.append(
                    HEADER_CONTENT_TYPE,
                    HEADER_JSON
                )
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
                headers.append(
                    HEADER_CONTENT_TYPE,
                    HEADER_JSON
                )
            }
        }
}