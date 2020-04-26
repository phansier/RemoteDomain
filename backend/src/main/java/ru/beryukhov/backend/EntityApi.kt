package ru.beryukhov.backend

import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Result

interface EntityApi {
    suspend fun create(entity: Entity): Result<Entity>

    suspend fun get(): Result<List<Entity>>
    suspend fun update(entity: Entity): Result<Entity>
    suspend fun delete(entity: Entity): CompletableResult
}