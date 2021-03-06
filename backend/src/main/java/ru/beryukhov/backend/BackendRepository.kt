package ru.beryukhov.backend

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import ru.beryukhov.common.ApiRequest
import ru.beryukhov.common.Create
import ru.beryukhov.common.model.*
import ru.beryukhov.common.tree_diff.plus

/**
 * Created by Andrey Beryukhov
 */

@ExperimentalCoroutinesApi
class BackendRepository(
    private val entityRepository: EntityRepository
) : EntityApi by entityRepository

data class TestUser(val id: String, val userName: String) {
    val entity get() = Pair(id, Entity(leaf = userName))

    constructor(id: String, entity: Entity) : this(id, entity.leaf ?: "")
}

data class TestPost(val id: String, val userId: String, val message: String) {
    companion object {
        private const val USER_ID = "UserId"
        private const val MESSAGE = "Message"
    }

    val entity
        get() = Pair(
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
class EntityRepository(private val broadcastChannel: BroadcastChannel<ApiRequest>) :
    EntityApi {
    @Volatile
    private var nextUserId: Long = 0

    private val testUser = TestUser("0", "TestaTestovna")
    private val testPost = TestPost("0", "0", "Coronavirus")

    private var entity =
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


    override suspend fun post(diff: Entity, clientId: String): Result<Entity> {
        if (!diff.validate(entity, clientId))
            return Failure((ForbiddenClientId("Diff contains forbidden client ids")))
        entity += diff
        broadcastChannel.offer(ApiRequest(method = Create, entity = Entity::class))
        return Success(entity)
    }

    override suspend fun get(clientId: String): Result<Entity> {
        val result = entity.filter(clientId)
        if (result != null) {
            return Success(result)
        } else {
            return Failure(Forbidden("For this client entity is not available"))
        }
    }

    override suspend fun getClientId(): Result<String> {
        synchronized(this) {
            nextUserId++
            return Success(nextUserId.toString())
        }
    }

    /*override suspend fun getPostsDiff(from: Long, to: Long): Result<Diff<List<Post>>> {

    }*/

}

