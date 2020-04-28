package ru.beryukhov.remote_domain.domain

import ru.beryukhov.common.model.Entity
import java.io.Serializable

data class User(val id: String, val userName: String) : Serializable {
    val entity get() = Pair(id, Entity(leaf = userName))

    val createDiff get() = diff(entity)

    private fun diff(pair: Pair<String, Entity?>): Entity {
        return Entity(
            mapOf(
                "User" to Entity(
                    mapOf(
                        pair
                    )
                )
            )
        )
    }

    constructor(id: String, entity: Entity) : this(id, entity.leaf ?: "")
}
