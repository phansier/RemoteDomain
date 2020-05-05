package ru.beryukhov.client_lib

const val DEFAULT_CLIENT_ID = "-1"

expect class LibState {
    fun saveLibFirstInit()
    fun getLibFirstInit(): Boolean
    fun getClientId(): String
    fun setClientId(id: String)
    fun getAndIncrementId(): Long
    fun generateAndSaveEncodedPassword(): String
    fun getEncodedPassword(): String?
}