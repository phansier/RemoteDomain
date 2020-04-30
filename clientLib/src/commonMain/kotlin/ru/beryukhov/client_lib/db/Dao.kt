package ru.beryukhov.client_lib.db

import kotlinx.coroutines.flow.Flow

interface Dao<Entity>{
    fun update(entity: Entity)
    fun getEntity(): Entity
    fun getEntityFlow(): Flow<Entity>
    fun createTable()
    fun deleteTable()
}