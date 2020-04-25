package ru.beryukhov.client_lib.db

import ru.beryukhov.common.model.Entity
import kotlin.reflect.KClass

interface DaoStorage{
    fun addDao(entity: KClass<out Entity>, dao: Dao<out Entity>)
    fun getDao(entity: KClass<out Entity>): Dao<out Entity>?
}