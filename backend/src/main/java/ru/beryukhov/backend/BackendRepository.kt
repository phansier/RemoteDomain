package ru.beryukhov.backend

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import ru.beryukhov.common.*
import ru.beryukhov.common.model.*
import ru.beryukhov.common.model.Error

/**
 * Created by Andrey Beryukhov
 */

@ExperimentalCoroutinesApi
class BackendRepository(
    private val entityRepository: EntityRepository
) :  Backend,
    EntityApi by entityRepository
@ExperimentalCoroutinesApi
class EntityRepository(private val broadcastChannel: BroadcastChannel<ApiRequest>) : EntityApi {
    @Volatile
    private var nextUserId: Int = 0

    private val entities = mutableListOf<Entity>(
        Entity(mapOf(
            "Users" to Entity(mapOf(
                "-1" to Entity(leaf= "TestTestov")
            )),
            "Posts" to Entity(mapOf(
                "-1" to Entity(mapOf(
                    "Message" to Entity(leaf ="Hello world"),
                    "UserName" to Entity(leaf ="-1")
                ))
            ))
        ))
    )

    override suspend fun create(entity: Entity): Result<Entity> {
        entities.add(entity)
        broadcastChannel.offer(ApiRequest(method = Create, entity = Entity::class))
        return Success(entity)
    }

    override suspend fun get(): Result<List<Entity>> {
        return Success(entities.toList())
    }

    /*override suspend fun getPostsDiff(from: Long, to: Long): Result<Diff<List<Post>>> {

    }*/

    override suspend fun update(entity: Entity): Result<Entity> {
        broadcastChannel.offer(ApiRequest(method = Update, entity = Entity::class))
        TODO("not implemented")
    }

    override suspend fun delete(entity: Entity): CompletableResult {
        return if (entities.remove(entity)) {
            broadcastChannel.offer(ApiRequest(method = Delete, entity = Entity::class))
            CompletableResult.Success
        } else CompletableResult.Failure(
            Error.NoSuchElementError("todo")
        )
    }

}

