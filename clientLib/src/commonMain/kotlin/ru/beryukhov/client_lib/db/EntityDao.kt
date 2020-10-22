package ru.beryukhov.client_lib.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.beryukhov.client_lib.QueryWrapper
import ru.beryukhov.client_lib.log
import ru.beryukhov.clientlib.DbEntity
import ru.beryukhov.clientlib.DomainQueries
import ru.beryukhov.common.model.Entity
import kotlin.coroutines.CoroutineContext

expect class EntityDao : Dao<Entity>

internal class EntityDaoImpl(
    sqlDriver: SqlDriver,
    private val dbContext: CoroutineContext
) : Dao<Entity> {
    private val domainQueries: DomainQueries = QueryWrapper(sqlDriver).domainQueries

    override fun createTable() {
        CoroutineScope(dbContext).launch {
            log("EntityDao", "createDb() start")
            domainQueries.createDbEntityTable()
            domainQueries.insertDbEntity(Entity().toJson())
            log("EntityDao", "createDb() created")
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

    override fun getEntityJson(): String {
        return domainQueries.selectAll().executeAsOne().json!!
    }

    override fun updateJson(json: String) {
        CoroutineScope(dbContext).launch {
            //SQLite starts ID on insert from 1 but if no insert were made during the session returns 0 as a lastInsertId()
            //val id = diffQueries.lastInsertId().executeAsOne()
            val id = 1L
            domainQueries.updateDbEntity(id, json)
        }
    }

}

expect fun Entity.toJson(): String

expect fun DbEntity.toEntity(): Entity