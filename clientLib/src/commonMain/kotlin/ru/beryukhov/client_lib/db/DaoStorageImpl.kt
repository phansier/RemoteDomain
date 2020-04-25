package ru.beryukhov.client_lib.db

import ru.beryukhov.common.model.Entity
import kotlin.reflect.KClass

/**
 * Created by Andrey Beryukhov
 */

class DaoStorageImpl(): DaoStorage {
    private val daos = mutableMapOf<KClass<out Entity>, Dao<out Entity>>()

    override fun addDao(entity: KClass<out Entity>, dao: Dao<out Entity>) {
        synchronized(this){
            daos.put(entity, dao)
        }
    }
    override fun getDao(entity: KClass<out Entity>): Dao<out Entity>? {
        synchronized(this){
            return daos[entity]
        }
    }
}
