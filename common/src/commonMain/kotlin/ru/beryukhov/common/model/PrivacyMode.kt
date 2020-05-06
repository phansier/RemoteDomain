package ru.beryukhov.common.model

import ru.beryukhov.common.tree_diff.DiffEntity

enum class PrivacyMode {
    PRIVATE, // can be visible only by creator
    READ, // can be updated only by creator but visible by anyone
    UPDATE // can be updated by any
}

fun Entity.validate(diff: Entity?, clientId: String): Boolean {
    if (privacyMode != PrivacyMode.UPDATE && clientId != creatorClientId) {
        return false
    }
    diff?.data?.entries?.forEach {
        if (data?.containsKey(it.key) == true && !data[it.key]!!.validate(it.value, clientId)) {
            return false
        }
    }
    return true
}

fun Entity.filter(clientId: String): Entity? {
    if (privacyMode == PrivacyMode.PRIVATE && clientId != creatorClientId) {
        return null
    }
    val result = DiffEntity(
        data = mutableMapOf(),
        leaf = leaf,
        creatorClientId = creatorClientId,
        privacyMode = privacyMode
    )
    data?.entries?.forEach {
        result.data!![it.key] = it.value?.filter(clientId)
    }
    return result.entity
}