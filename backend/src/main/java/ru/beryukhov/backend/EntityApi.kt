package ru.beryukhov.backend

import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Result

interface EntityApi {
    suspend fun post(diff: Entity, clientId: String): Result<Entity>
    suspend fun get(clientId: String): Result<Entity>
    suspend fun getClientId(): Result<String>
}