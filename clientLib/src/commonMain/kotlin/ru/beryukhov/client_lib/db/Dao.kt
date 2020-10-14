package ru.beryukhov.client_lib.db

import kotlinx.coroutines.flow.Flow

interface Dao<Entity>{
    fun update(entity: Entity)
    fun updateJson(json: String)
    fun getEntity(): Entity
    fun getEntityJson(): String
    fun getEntityFlow(): Flow<Entity>
    fun createTable()
}