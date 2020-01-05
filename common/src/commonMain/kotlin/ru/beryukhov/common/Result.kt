package ru.beryukhov.common

/**
 * Created by Andrey Beryukhov
 */
//Kotlin Result can not be used as a return type
sealed class Result<T> {
    class Success<T>(val value: T) : Result<T>()
    class Failure<T>(val exception: Throwable) : Result<T>()
}

sealed class CompletableResult{
    object Success : CompletableResult()
    class Failure(val exception: Throwable) : CompletableResult()
}