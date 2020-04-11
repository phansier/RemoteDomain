package ru.beryukhov.client_lib.db

import kotlinx.coroutines.flow.Flow

interface Dao<Entity>{
    fun insert(entity: Entity)
    fun delete(id: String)
    fun getEntities(): List<Entity>
    fun getEntitiesFlow(): Flow<List<Entity>>
    fun createTable()
    fun deleteTable()
}