package ru.beryukhov.common.model

data class Entity(
    val data: Map<String, Entity>? = null,
    val leaf: String? = null
)

/*
data class Structure(val name: String, val data: Entity): Entity()

data class DataLeaf<T>(val data: T): Entity()
*/

