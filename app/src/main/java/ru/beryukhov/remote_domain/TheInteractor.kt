package ru.beryukhov.remote_domain

import android.app.Application
import ru.beryukhov.client_lib.RemoteDomainClient

class TheInteractor(val applicationContext: Application) {
    val remoteDomainClient: RemoteDomainClient by lazy {
        RemoteDomainClient(applicationContext).apply {
            init(
                serverUrl = SERVER_URL,
                socketUrl = SOCKET_URL,
                logRequests = BuildConfig.DEBUG
            )
        }
    }
}