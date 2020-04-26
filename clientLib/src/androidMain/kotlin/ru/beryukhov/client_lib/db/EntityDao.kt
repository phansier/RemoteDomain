package ru.beryukhov.client_lib.db

import android.content.Context
import com.google.gson.Gson
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import ru.beryukhov.client_lib.DbEntity
import ru.beryukhov.client_lib.QueryWrapper
import ru.beryukhov.common.model.Entity

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
actual class EntityDao constructor(context: Context) : Dao<Entity> by EntityDaoImpl(
    sqlDriver = AndroidSqliteDriver(
        QueryWrapper.Schema,
        context,
        "test.db"
    ),
    dbContext = newSingleThreadContext("DB")
)

private val gson by lazy { Gson() }

actual fun Entity.toJson(): String {
    return gson.toJson(this)
}

actual fun DbEntity.toEntity(): Entity {
    return gson.fromJson(this.json, Entity::class.java)
}