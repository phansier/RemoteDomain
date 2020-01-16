package ru.beryukhov.common.model

sealed class CompletableResult {
    object Success : CompletableResult()
    class Failure(val error: Error) : CompletableResult()
}