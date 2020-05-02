package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.Success

class ClientApiImpl(
    private val httpClient: HttpClient,
    private val serverUrl: String
) : BaseHttpClient(),
    ClientApi<Entity> {
    override suspend fun create(entity: Entity, endpoint: String): Result<Entity> =
        httpClient.makeRequest("post(\"$serverUrl/$endpoint\")") {
            post<Success<Entity>>("$serverUrl/$endpoint") {
                body = entity
                headers.append(
                    HEADER_CONTENT_TYPE,
                    HEADER_JSON
                )
            }
        }

    override suspend fun get(endpoint: String): Result<Entity> =
        httpClient.makeRequest("get(\"$serverUrl/$endpoint\")") {
            get<Success<Entity>>("$serverUrl/$endpoint")
        }

    override suspend fun getClientId(): Result<String> =
        httpClient.makeRequest("get(\"$serverUrl/clientid\")") {
            get<Success<String>>("$serverUrl/clientid")
        }
}