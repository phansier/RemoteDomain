package ru.beryukhov.common.model

import kotlinx.serialization.Serializable

/**
 * Created by Andrey Beryukhov
 */
@Serializable
sealed class Error {
    @Serializable
    class NoSuchElementError(val message: String) : Error()
}