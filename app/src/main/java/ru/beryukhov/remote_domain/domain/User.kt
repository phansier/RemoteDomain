package ru.beryukhov.remote_domain.domain

import ru.beryukhov.common.model.Entity

data class User(val id: String, val userName: String) {
    val entity get() = Pair(id, Entity(leaf = userName))
    constructor(id:String,entity: Entity) : this(id, entity.leaf?:"")
}
