package ru.beryukhov.client_lib.push

interface Push {
    fun startReceive(socketUrl: String, pushCallback: (Any) -> Unit)
}