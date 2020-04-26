package ru.beryukhov.client_lib

import android.util.Log

actual fun log(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.i(tag, message)
    }
}