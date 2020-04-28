package ru.beryukhov.remote_domain

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class TheApplication: Application() {
   val theInteractor: TheInteractor by lazy {
       TheInteractor(this)
   }
}

