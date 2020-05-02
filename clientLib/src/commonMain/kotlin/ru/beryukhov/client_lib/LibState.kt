package ru.beryukhov.client_lib

expect class LibState {
    fun saveLibFirstInit()
    fun getLibFirstInit(): Boolean
    fun getClientId(): String
    fun setClientId(id: String)
    fun getAndIncrementId(): Long
}