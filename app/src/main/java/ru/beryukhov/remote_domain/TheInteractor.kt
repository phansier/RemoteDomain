package ru.beryukhov.remote_domain

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import ru.beryukhov.client_lib.RemoteDomainClient

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class TheInteractor(val applicationContext: Application) {
    val remoteDomainClient: RemoteDomainClient by lazy{
        RemoteDomainClient(applicationContext).apply {
            firstInit()
        }
    }
}