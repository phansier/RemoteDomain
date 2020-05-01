package ru.beryukhov.common.tree_diff

import ru.beryukhov.common.model.Entity


fun Entity.getDiff(other: Entity): Entity? = DiffImpl.get(this, other)
fun Entity.applyDiff(diff: Entity?): Entity = DiffImpl.apply(this, diff)

operator fun Entity.minus(other: Entity): Entity? = this.getDiff(other)
operator fun Entity.plus(diff: Entity?): Entity = this.applyDiff(diff)
