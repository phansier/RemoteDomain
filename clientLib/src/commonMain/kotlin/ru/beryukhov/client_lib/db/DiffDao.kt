package ru.beryukhov.client_lib.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.beryukhov.client_lib.DiffQueries
import ru.beryukhov.client_lib.EntityDiff
import ru.beryukhov.client_lib.QueryWrapper
import ru.beryukhov.client_lib.log
import ru.beryukhov.common.model.Entity
import kotlin.coroutines.CoroutineContext

expect class DiffDao : Dao<Entity>

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class DiffDaoImpl(
    sqlDriver: SqlDriver,
    private val dbContext: CoroutineContext
) : Dao<Entity> {
    private val diffQueries: DiffQueries = QueryWrapper(sqlDriver).diffQueries

    override fun createTable() {
        CoroutineScope(dbContext).launch {
            log("EntityDao", "createDb() start")
            diffQueries.deleteEntityDiffTable()
            diffQueries.createEntityDiffTable()
            diffQueries.insertEntityDiff(Entity().toJson())
            log("EntityDao", "createDb() created")
        }
    }

    override fun deleteTable() {
        CoroutineScope(dbContext).launch {
            log("EntityDao", "deleteDb() start")
            diffQueries.deleteEntityDiffTable()
            log("EntityDao", "deleteDb() deleted")
        }
    }

    override fun getEntityFlow(): Flow<Entity> {
        return diffQueries.selectAll().asFlow()
            .mapToOne(dbContext)
            .flowOn(dbContext)
            .conflate()
            .map { dbEntity -> dbEntity.toEntity() }
    }

    override fun getEntity(): Entity {
        return diffQueries.selectAll().executeAsOne().toEntity()
    }

    override fun update(entity: Entity) {
        CoroutineScope(dbContext).launch {
            //SQLite starts ID on insert from 1 but if no insert were made during the session returns 0 as a lastInsertId()
            //val id = diffQueries.lastInsertId().executeAsOne()
            val id = 1L
            diffQueries.updateEntityDiff(id, entity.toJson())
        }
    }
}

expect fun EntityDiff.toEntity(): Entity