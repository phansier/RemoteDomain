package ru.beryukhov.remote_domain.domain

import ru.beryukhov.common.model.Entity
import java.io.Serializable


data class Post(val id: String, val userId: String, val message: String) : Serializable {
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

    val createDiff get() = diff(entity)

    val updateDiff get() = diff(entity)

    val deleteDiff get() = diff(id to null)

    private fun diff(pair: Pair<String, Entity?>): Entity {
        return Entity(
            mapOf(
                "Post" to Entity(
                    mapOf(
                        pair
                    )
                )
            )
        )
    }

    constructor(id: String, entity: Entity) : this(
        id,
        entity.data?.get(USER_ID)?.leaf ?: "",
        entity.data?.get(MESSAGE)?.leaf ?: ""
    )
}