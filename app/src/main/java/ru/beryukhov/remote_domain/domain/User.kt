package ru.beryukhov.remote_domain.domain

import ru.beryukhov.common.model.Entity
import java.io.Serializable

data class User(val id: String, val userName: String) : Serializable {
    companion object {
        const val USER = "User"
    }

    /**
     * A getter property for mapping User into Pair of Id and Entity
     */
    private val entity get() = Pair(id, Entity(leaf = userName))

    /**
     * A constructor of User instance with Id and Entity parameters
     */
    constructor(id: String, entity: Entity) : this(id, entity.leaf ?: "")

    /**
     * Common function for creation of generic diff
     */
    private fun diff(pair: Pair<String, Entity?>): Entity {
        return Entity(
            mapOf(
                USER to Entity(
                    mapOf(
                        pair
                    )
                )
            )
        )
    }

    /**
     * A getter properties for create/update/delete operations
     */
    val createDiff get() = diff(entity)
    val updateDiff get() = diff(entity)
    val deleteDiff get() = diff(id to null)

}
