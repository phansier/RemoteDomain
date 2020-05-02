package ru.beryukhov.client_lib

import android.app.Application
import android.content.Context

private const val PREF_NAME = "lib_state"
private const val FIRST_INIT = "first_init"

actual class LibState(private val appContext: Application) {

    private val preferences by lazy {
        appContext.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
    }

    actual fun saveLibFirstInit() = preferences.edit()
        .putBoolean(FIRST_INIT, true)
        .apply()

    actual fun getLibFirstInit(): Boolean = preferences.getBoolean(FIRST_INIT, false)
}