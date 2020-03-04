package ru.beryukhov.remote_domain.push

interface Push {
    fun startReceive(socketUrl: String, log: suspend (String) -> Unit, pushCallback: (Any) -> Unit)
}