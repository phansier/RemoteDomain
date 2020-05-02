package ru.beryukhov.client_lib

expect class LibState {
    fun saveLibFirstInit()
    fun getLibFirstInit(): Boolean
}