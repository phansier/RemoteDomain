package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import ru.beryukhov.common.model.Entity
import kotlin.reflect.KClass

interface HttpClientRepository{
    fun addClientApi(entity: KClass<out Entity>, clientApi: ClientApi<out Entity>)
    fun getClientApi(entity: KClass<out Entity>): ClientApi<out Entity>?
    val httpClient: HttpClient
}