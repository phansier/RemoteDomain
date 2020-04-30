package ru.beryukhov.backend

import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Result

interface EntityApi {
    suspend fun post(entity: Entity): Result<Entity>
    suspend fun get(): Result<Entity>
}