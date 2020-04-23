package ru.beryukhov.remote_domain.domain

import ru.beryukhov.common.model.Entity

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