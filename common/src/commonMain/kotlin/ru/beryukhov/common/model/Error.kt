package ru.beryukhov.common.model

/**
 * Created by Andrey Beryukhov
 */
sealed class Error {
    class NoSuchElementError(val message: String) : Error()
}