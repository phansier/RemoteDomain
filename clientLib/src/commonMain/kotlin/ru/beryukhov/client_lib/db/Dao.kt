package ru.beryukhov.client_lib.db

import kotlinx.coroutines.flow.Flow

interface Dao<Entity>{
    fun insert(entity: Entity)
    fun delete(id: Long)
    fun getEntities(): List<Entity>
    fun getEntitiesFlow(): Flow<List<Entity>>
    fun getEntity(): Entity?
    fun getEntityFlow(): Flow<Entity?>
    fun createTable()
    fun deleteTable()
}