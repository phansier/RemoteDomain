package ru.beryukhov.client_lib.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
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

    override fun getEntitiesFlow(): Flow<List<Entity>> {
        return domainQueries.selectAll().asFlow()
            .mapToList(dbContext)
            .flowOn(dbContext)
            .conflate()
            .map { list -> list.map { dbEntity -> dbEntity.toEntity() } }
    }

    override fun getEntityFlow(): Flow<Entity?> {
        return domainQueries.selectAll().asFlow()
            .mapToOneOrNull(dbContext)
            .flowOn(dbContext)
            .conflate()
            .map { dbEntity -> dbEntity?.toEntity() }
    }

    override fun getEntities(): List<Entity> {
        return domainQueries.selectAll().executeAsList().map { dbEntity -> dbEntity.toEntity() }
    }

    override fun getEntity(): Entity? {
        return domainQueries.selectAll().executeAsOneOrNull()?.toEntity()
    }

    override fun delete(id: Long) = domainQueries.deleteDbEntity(id)

    override fun insert(entity: Entity) {
        CoroutineScope(dbContext).launch {
            domainQueries.insertDbEntity(entity.toJson())
        }
    }
}

expect fun Entity.toJson(): String

expect fun DbEntity.toEntity(): Entity