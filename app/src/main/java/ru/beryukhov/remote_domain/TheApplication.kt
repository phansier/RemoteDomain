package ru.beryukhov.remote_domain

import android.app.Application

class TheApplication: Application() {
   val theInteractor: TheInteractor by lazy {
       TheInteractor(this)
   }
}

