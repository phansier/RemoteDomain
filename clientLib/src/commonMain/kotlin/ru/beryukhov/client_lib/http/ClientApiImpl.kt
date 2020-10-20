package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.Success

class ClientApiImpl(
    private val httpClient: HttpClient,
    private val serverUrl: String
) : BaseHttpClient(),
    ClientApi<Entity> {
    override suspend fun create(entity: Entity, credentials: Credentials): Result<Entity> =
        httpClient.makeRequest("post(\"$serverUrl/entity\")") {
            post<Success<Entity>>("$serverUrl/entity") {
                body = entity
                headers.append(
                    ContentType,
                    HEADER_JSON
                )
                headers[HttpHeaders.Authorization] = constructBasicAuthValue(credentials)
            }
        }

    override suspend fun get(credentials: Credentials): Result<Entity> =
        httpClient.makeRequest("get(\"$serverUrl/entity\")") {
            get<Success<Entity>>("$serverUrl/entity"){
                headers[HttpHeaders.Authorization] = constructBasicAuthValue(credentials)
            }
        }

    override suspend fun getClientId(passwordEncoded: String): Result<String> =
        httpClient.makeRequest("get(\"$serverUrl/clientid\")") {
            get<Success<String>>("$serverUrl/clientid?passwordEncoded=$passwordEncoded")
        }

    //taken from io.ktor.client.features.auth.providers.BasicAuthProvider
    @OptIn(InternalAPI::class)
    internal fun constructBasicAuthValue(credentials: Credentials): String {
        val authString = "${credentials.clientId}:${credentials.encodedPassword}"
        val authBuf = authString.toByteArray().encodeBase64()

        return "Basic $authBuf"
    }
}