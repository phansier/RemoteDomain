package ru.beryukhov.client_lib.http

import ru.beryukhov.common.model.Result


interface ClientApi<Entity> {
    suspend fun create(entity: Entity, endpoint: String): Result<Entity>
    suspend fun get(endpoint: String): Result<Entity>
    suspend fun getClientId(): Result<String>
}