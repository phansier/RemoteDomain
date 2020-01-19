package ru.beryukhov.common.model

import kotlinx.serialization.Serializable

@Serializable
sealed class CompletableResult {
    @Serializable
    object Success : CompletableResult()

    @Serializable
    class Failure(val error: Error) : CompletableResult()
}