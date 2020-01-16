package ru.beryukhov.common.model

import kotlinx.serialization.Serializable

/**
 * Created by Andrey Beryukhov
 */
@Serializable
data class Post(val id: String, val userId: String, val message: String)