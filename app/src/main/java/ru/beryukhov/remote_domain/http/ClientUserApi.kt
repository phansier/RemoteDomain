package ru.beryukhov.remote_domain.http

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import ru.beryukhov.client_lib.http.BaseHttpClient
import ru.beryukhov.client_lib.http.ClientApi
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.User

class ClientUserApi(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val log: suspend (String) -> Unit
) : BaseHttpClient(),
    ClientApi<User> {
    override suspend fun create(entity: User, endpoint: String): Result<User> =
        httpClient.makeRequest("post(\"$serverUrl/$endpoint\")", log) {
            post<Result.Success<User>>("$serverUrl/$endpoint") {
                body = entity
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

    override suspend fun get(endpoint: String): Result<List<User>> =
        httpClient.makeRequest("get(\"$serverUrl/$endpoint\")", log) {
            get<Result.Success<List<User>>>("$serverUrl/$endpoint")
        }

    override suspend fun update(entity: User, endpoint: String): Result<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delete(entity: User, endpoint: String): CompletableResult =
        httpClient.makeCompletableRequest("delete(\"$serverUrl/$endpoint\")", log) {
            delete("$serverUrl/$endpoint") {
                body = entity
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }
}