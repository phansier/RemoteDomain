package ru.beryukhov.common.model

/**
 * Created by Andrey Beryukhov
 */
sealed class Error

class NoSuchElementError(val message: String) : Error()
class InvalidParam(val message: String) : Error()

class HttpError(val httpStatusCode: Int) : Error()
