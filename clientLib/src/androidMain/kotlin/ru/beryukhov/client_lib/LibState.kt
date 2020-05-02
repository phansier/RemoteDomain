package ru.beryukhov.client_lib

import android.app.Application
import android.content.Context

private const val PREF_NAME = "lib_state"
private const val FIRST_INIT = "first_init"
private const val CLIENT_ID = "client_id"
private const val ENTITY_ID = "entity_id"

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

    actual fun getClientId(): String = preferences.getString(CLIENT_ID, "-1")!!

    actual fun setClientId(id: String) = preferences.edit()
    .putString(CLIENT_ID, id)
    .apply()

    actual fun getAndIncrementId(): Long {
        synchronized(this){
            val id = preferences.getLong(ENTITY_ID, 0)
            preferences.edit().putLong(ENTITY_ID, id+1).apply()
            return id
        }
    }
}