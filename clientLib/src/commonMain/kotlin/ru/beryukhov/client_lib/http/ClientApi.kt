package ru.beryukhov.client_lib.http

import ru.beryukhov.common.model.Result


interface ClientApi<Entity> {
    suspend fun create(entity: Entity, credentials: Credentials): Result<Entity>
    suspend fun get(credentials: Credentials): Result<Entity>
    suspend fun getClientId(passwordEncoded: String): Result<String>
}

data class Credentials(val clientId:String, val encodedPassword: String)