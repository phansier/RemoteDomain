package ru.beryukhov.common.model

data class Entity(
    val data: Map<String, Entity?>? = null, //Entity in Map should be null only in diff
    val leaf: String? = null,
    val creatorClientId: String? = null,
    val privacyMode: PrivacyMode = PrivacyMode.UPDATE
)
