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
) : Backend,
    EntityApi by entityRepository

data class User(val id: String, val userName: String) {
    val entity get() = Pair(id, Entity(leaf = userName))
    constructor(id:String,entity: Entity) : this(id, entity.leaf?:"")
}

data class Post(val id: String, val userId: String, val message: String) {
    companion object{
        private const val USER_ID = "UserId"
        private const val MESSAGE = "Message"
    }
    val entity get() = Pair(
        id, Entity(
            mapOf(
                USER_ID to Entity(leaf = userId),
                MESSAGE to Entity(leaf = message)
            )
        )
    )

    constructor(id: String, entity: Entity) : this(
        id,
        entity.data?.get(USER_ID)?.leaf ?: "",
        entity.data?.get(MESSAGE)?.leaf ?: ""
    )
}

@ExperimentalCoroutinesApi
class EntityRepository(private val broadcastChannel: BroadcastChannel<ApiRequest>) : EntityApi {
    @Volatile
    private var nextUserId: Int = 0

    private val testUser = User("0", "TestaTestovna")
    private val testPost = Post("0", "0", "Coronavirus")

    private val entities = mutableListOf<Entity>(
        Entity(
            mapOf(
                "User" to Entity(
                    mapOf(
                        "-1" to Entity(leaf = "TestTestov"),
                        testUser.entity
                    )
                ),
                "Post" to Entity(
                    mapOf(
                        "-1" to Entity(
                            mapOf(
                                "UserId" to Entity(leaf = "-1"),
                                "Message" to Entity(leaf = "Hello world")
                            )
                        ),
                        testPost.entity
                    )
                )
            )
        )
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

