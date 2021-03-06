package ru.beryukhov.remote_domain.domain

import ru.beryukhov.common.model.Entity
import java.io.Serializable


data class Post(
    val id: String,
    val userId: String,
    val message: String
) : Serializable {
    companion object {
        const val POST = "Post"
        private const val USER_ID = "UserId"
        private const val MESSAGE = "Message"
    }

    private val entity
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

    private fun diff(pair: Pair<String, Entity?>): Entity {
        return Entity(
            mapOf(
                POST to Entity(
                    mapOf(
                        pair
                    )
                )
            )
        )
    }

    val createDiff get() = diff(entity)
    val updateDiff get() = diff(entity)
    val deleteDiff get() = diff(id to null)

}