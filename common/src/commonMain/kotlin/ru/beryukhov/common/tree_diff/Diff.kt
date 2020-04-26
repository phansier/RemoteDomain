package ru.beryukhov.common.tree_diff

import ru.beryukhov.common.model.Entity

interface Diff {
    //Entity-Entity = Entity(Diff)
    fun get(a: Entity, b: Entity): Entity?

    //Entity+Entity(Diff) = Entity
    fun apply(e: Entity, diff: Entity): Entity
}

object DiffImpl : Diff {
    override fun get(a: Entity, b: Entity): Entity? {
        return recGet(a, b)
    }

    private fun recGet(a: Entity, b: Entity): Entity? {
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
                val dataDiff = recGet(a.data[it]!!, b.data[it]!!)
                if (dataDiff != null) {
                    diff.data?.put(it, dataDiff)
                }
            }
        }
        return diff.entity
    }

    override fun apply(e: Entity, diff: Entity): Entity {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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