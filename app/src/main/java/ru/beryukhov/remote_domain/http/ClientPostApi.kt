package ru.beryukhov.remote_domain.http

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import ru.beryukhov.client_lib.http.BaseHttpClient
import ru.beryukhov.client_lib.http.ClientApi
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.Result

class ClientPostApi(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val log: suspend (String) -> Unit
) : BaseHttpClient(),
    ClientApi<Post> {
    override suspend fun create(entity: Post, endpoint: String): Result<Post> =
        httpClient.makeRequest("post(\"$serverUrl/$endpoint\")", log) {
            post<Result.Success<Post>>("$serverUrl/$endpoint") {
                body = entity
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

    override suspend fun get(endpoint: String): Result<List<Post>> =
        httpClient.makeRequest("get(\"$serverUrl/$endpoint\")", log) {
            get<Result.Success<List<Post>>>("$serverUrl/$endpoint")
        }

    override suspend fun update(entity: Post, endpoint: String): Result<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delete(entity: Post, endpoint: String): CompletableResult =
        httpClient.makeCompletableRequest("delete(\"$serverUrl/$endpoint\")", log) {
            delete("$serverUrl/$endpoint") {
                body = entity
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }
}