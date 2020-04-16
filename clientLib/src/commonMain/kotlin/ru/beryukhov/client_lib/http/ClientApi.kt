package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.Success


interface ClientApi<Entity>{

    suspend fun create(entity: Entity, endpoint: String): Result<Entity>
    suspend fun get(endpoint: String): Result<List<Entity>>
    suspend fun update(entity: Entity, endpoint: String): Result<Entity>
    suspend fun delete(entity: Entity, endpoint: String): CompletableResult
}


class ClientApiImpl(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val log: suspend (String) -> Unit
) : BaseHttpClient(), ClientApi<Entity> {
    override suspend fun create(entity: Entity, endpoint: String): Result<Entity> =
        httpClient.makeRequest("post(\"$serverUrl/$endpoint\")", log) {
            post<Success<Entity>>("$serverUrl/$endpoint") {
                body = entity
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

    override suspend fun get(endpoint: String): Result<List<Entity>> =
        httpClient.makeRequest("get(\"$serverUrl/$endpoint\")", log) {
            get<Success<List<Entity>>>("$serverUrl/$endpoint")
        }

    override suspend fun update(entity: Entity, endpoint: String): Result<Entity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delete(entity: Entity, endpoint: String): CompletableResult =
        httpClient.makeCompletableRequest("delete(\"$serverUrl/$endpoint\")", log) {
            delete("$serverUrl/$endpoint") {
                body = entity
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

}