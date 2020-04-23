package ru.beryukhov.client_lib.push

interface Push {
    fun startReceive(socketUrl: String, log: suspend (String) -> Unit, pushCallback: (Any) -> Unit)
}