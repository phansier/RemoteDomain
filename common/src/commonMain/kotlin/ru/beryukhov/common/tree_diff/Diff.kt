package ru.beryukhov.common.tree_diff

import ru.beryukhov.common.model.Entity

interface Diff {
    //Entity-Entity = Entity(Diff)
    fun get(a: Entity, b: Entity): Entity?

    //Entity+Entity(Diff) = Entity
    fun apply(e: Entity, diff: Entity?): Entity
}

object DiffImpl : Diff {
    override fun get(a: Entity, b: Entity): Entity? {
        if (a == b) return null //no changes
        if (a.data == b.data) return b.copy(data = null) //changes are in b.leaf

        val leaf = if (a.leaf == b.leaf) { //changes are in b.map
            null
        } else b.leaf

        if (a.data == null) {
            //added map to b
            return b.copy(leaf = leaf)
        }
        if (b.data == null) {
            //removed map from b
            return Entity(leaf = leaf)
        }
        val commonKeys = a.data.keys.plus(b.data.keys)
        val diff = DiffEntity(data = mutableMapOf(), leaf = leaf)
        commonKeys.forEach {
            if (!a.data.containsKey(it)) {
                //new key in b
                diff.data?.put(it, b.data.get(it))
            } else if (!b.data.containsKey(it)) {
                //removed key in b
                diff.data?.put(it, null)
            } else {
                //key is the same
                //changed value in b
                val dataDiff = get(a.data[it]!!, b.data[it]!!)
                if (dataDiff != null) {
                    diff.data?.put(it, dataDiff)
                }
            }
        }
        return diff.entity
    }

    override fun apply(e: Entity, diff: Entity?): Entity {
        if (diff == null) return e
        val result = DiffEntity(
            e.data?.toMutableMap(),
            diff.leaf ?: e.leaf
        )
        if (diff.data == null) return result.entity
        diff.data.keys.forEach {
            if (diff.data[it] == null) {
                //key was removed
                result.data?.remove(it)
            } else if (e.data?.containsKey(it) == true) {
                //apply diff to existing value
                result.data?.set(it, apply(e.data[it]!!, diff.data[it]))
            } else {
                //key was added
                result.data?.set(it, diff.data[it])
            }
        }
        return result.entity
    }
}

/**
 * Special case of Entity with mutable map for performance
 */
private data class DiffEntity(
    val data: MutableMap<String, Entity?>? = null, //Entity in Map should be null only in diff
    val leaf: String? = null
) {
    val entity get() = Entity(if (data.isNullOrEmpty()) null else data, leaf)
}