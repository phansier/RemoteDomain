package ru.beryukhov.client_lib

import RN
import android.app.Application
import io.ktor.util.InternalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import ru.beryukhov.client_lib.db.DiffDao
import ru.beryukhov.client_lib.db.EntityDao

@InternalAPI
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
actual class RemoteDomainClient constructor(applicationContext: Application) :
    RemoteDomainClientApi by RemoteDomainClientImpl(
        EntityDao(applicationContext),
        DiffDao(applicationContext),
        LibState(applicationContext),
        RN(applicationContext)
    )