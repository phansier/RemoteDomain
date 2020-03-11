package ru.beryukhov.remote_domain.http

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import ru.beryukhov.common.UserApi
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.User

class ClientUserApi(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val log: suspend (String) -> Unit
) : BaseHttpClient(),
    UserApi {
    override suspend fun createUser(userName: String): Result<User> =
        httpClient.makeRequest("post<Result.Success<User>>(\"$serverUrl/user\")", log) {
            post<Result.Success<User>>("$serverUrl/user") {
                body = User("0", userName)
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

    override suspend fun getUsers(): Result<List<User>> =
        httpClient.makeRequest("get<Result.Success<List<User>>(\"$serverUrl/user\")", log) {
            get<Result.Success<List<User>>>("$serverUrl/user")
        }

    override suspend fun updateUser(user: User): Result<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deleteUser(user: User): CompletableResult =
        httpClient.makeCompletableRequest("delete(\"$serverUrl/user\")", log) {
            delete("$serverUrl/user") {
                body = user
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

}