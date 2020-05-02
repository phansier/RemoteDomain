package ru.beryukhov.client_lib.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.beryukhov.common.model.Entity
import ru.beryukhov.client_lib.DbEntity
import ru.beryukhov.client_lib.DomainQueries
import ru.beryukhov.client_lib.QueryWrapper
import ru.beryukhov.client_lib.log
import kotlin.coroutines.CoroutineContext

expect class EntityDao : Dao<Entity>

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class EntityDaoImpl(
    sqlDriver: SqlDriver,
    private val dbContext: CoroutineContext
) : Dao<Entity> {
    private val domainQueries: DomainQueries = QueryWrapper(sqlDriver).domainQueries

    override fun createTable() {
        CoroutineScope(dbContext).launch {
            log("EntityDao", "createDb() start")
            domainQueries.deleteDbEntityTable()
            domainQueries.createDbEntityTable()
            domainQueries.insertDbEntity(Entity().toJson())
            log("EntityDao", "createDb() created")
        }
    }

    override fun deleteTable() {
        CoroutineScope(dbContext).launch {
            log("EntityDao", "deleteDb() start")
            domainQueries.deleteDbEntityTable()
            log("EntityDao", "deleteDb() deleted")
        }
    }

    override fun getEntityFlow(): Flow<Entity> {
        return domainQueries.selectAll().asFlow()
            .mapToOne(dbContext)
            .flowOn(dbContext)
            .conflate()
            .map { dbEntity -> dbEntity.toEntity() }
    }

    override fun getEntity(): Entity {
        return domainQueries.selectAll().executeAsOne().toEntity()
    }

    override fun update(entity: Entity) {
        CoroutineScope(dbContext).launch {
            //SQLite starts ID on insert from 1 but if no insert were made during the session returns 0 as a lastInsertId()
            //val id = domainQueries.lastInsertId().executeAsOne()
            val id = 1L
            domainQueries.updateDbEntity(id, entity.toJson())
        }
    }
}

expect fun Entity.toJson(): String

expect fun DbEntity.toEntity(): Entity