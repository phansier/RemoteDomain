package ru.beryukhov.common.model

/**
 * Created by Andrey Beryukhov
 */
//Kotlin Result can not be used as a return type
sealed class Result<T>

class Success<T>(val value: T) : Result<T>()

class Failure<T>(val error: Error) : Result<T>()


