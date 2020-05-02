package ru.beryukhov.remote_domain.domain

import android.util.Log
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

fun getIdFromToString(s: String):String{
    val regex = Regex("(?<=id=).*(?=,)")
    return regex.find(s)!!.value
}
