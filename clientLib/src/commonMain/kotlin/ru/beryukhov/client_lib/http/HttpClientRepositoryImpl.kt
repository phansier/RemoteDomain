package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import ru.beryukhov.common.model.Entity
import kotlin.reflect.KClass


expect fun createHttpClient(logRequests: Boolean): HttpClient

class HttpClientRepositoryImpl(private val serverUrl: String, logRequests: Boolean, private val log: suspend (String) -> Unit) :
    HttpClientRepository {

    override val httpClient: HttpClient by lazy { createHttpClient(logRequests) }

    private val apis = mutableMapOf<KClass<out Entity>, ClientApi<out Entity>>()

    override fun addClientApi(entity: KClass<out Entity>, clientApi: ClientApi<out Entity>) {
        synchronized(this){
            apis.put(entity, clientApi)
        }
    }

    override fun getClientApi(entity: KClass<out Entity>): ClientApi<out Entity>? {
        synchronized(this){
            return apis[entity]
        }
    }

    /*override val clientApi: ClientApi<Entity> by lazy {
        ClientApiImpl(
            httpClient,
            serverUrl,
            log
        )
    }*/

}