package ru.beryukhov.common.model

sealed class CompletableResult
object CompletableSuccess : CompletableResult()
class CompletableFailure(val error: Error) : CompletableResult()