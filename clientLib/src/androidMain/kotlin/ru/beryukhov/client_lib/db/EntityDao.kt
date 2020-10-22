package ru.beryukhov.client_lib.db

import android.content.Context
import com.google.gson.Gson
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import ru.beryukhov.client_lib.QueryWrapper
import ru.beryukhov.clientlib.DbEntity
import ru.beryukhov.clientlib.EntityDiff
import ru.beryukhov.common.model.Entity

actual class EntityDao constructor(context: Context) : Dao<Entity> by EntityDaoImpl(
    sqlDriver = AndroidSqliteDriver(
        QueryWrapper.Schema,
        context,
        "test.db"
    ),
    dbContext = @OptIn(ObsoleteCoroutinesApi::class) newSingleThreadContext("DB")
)

actual class DiffDao constructor(context: Context): Dao<Entity> by DiffDaoImpl(
    sqlDriver = AndroidSqliteDriver(
        QueryWrapper.Schema,
        context,
        "test.db"
    ),
    dbContext = @OptIn(ObsoleteCoroutinesApi::class) newSingleThreadContext("DB")
)

private val gson by lazy { Gson() }

actual fun Entity.toJson(): String {
    return gson.toJson(this)
}

actual fun DbEntity.toEntity(): Entity {
    return gson.fromJson(this.json, Entity::class.java)
}

actual fun EntityDiff.toEntity(): Entity {
    return gson.fromJson(this.json, Entity::class.java)
}