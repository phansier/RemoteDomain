package ru.beryukhov.client_lib

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import ru.beryukhov.client_lib.db.EntityDao

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
actual class RemoteDomainClient constructor(applicationContext: Application) :
    RemoteDomainClientApi by RemoteDomainClientImpl(
        EntityDao(applicationContext)
    )