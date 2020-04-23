package ru.beryukhov.remote_domain.db

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.beryukhov.client_lib.db.Dao
import ru.beryukhov.common.model.Entity
import ru.beryukhov.remote_domain.*
import kotlin.coroutines.CoroutineContext

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class EntityDao(context: Context, private val log: suspend (String) -> Unit) :
    Dao<Entity> {
    private val domainQueries: DomainQueries
    private val dbContext: CoroutineContext

    private val gson by lazy { Gson() }

    init {
        val driver: SqlDriver =
            AndroidSqliteDriver(
                Database.Schema,
                context,
                "test.db"
            )
        val database = Database(driver)
        domainQueries = database.domainQueries
        Log.d("DR_", "EntityDao Init")
        dbContext = newSingleThreadContext("DB")
    }

    override fun createTable() {
        CoroutineScope(dbContext).launch {
            log("EntityDao:createDb() start")
            domainQueries.deleteDbEntityTable()
            domainQueries.createDbEntityTable()
            log("EntityDao:createDb() created")
        }
    }

    override fun deleteTable() {
        CoroutineScope(dbContext).launch {
            log("EntityDao:deleteDb() start")
            domainQueries.deleteDbEntityTable()
            log("EntityDao:deleteDb() deleted")
        }
    }

    override fun getEntitiesFlow(): Flow<List<Entity>> {
        return domainQueries.selectAll().asFlow()
            .mapToList(dbContext)
            .flowOn(dbContext)
            .conflate()
            .map { list -> list.map { dbEntity -> dbEntity.toEntity(gson) } }
    }

    override fun getEntityFlow(): Flow<Entity?> {
        return domainQueries.selectAll().asFlow()
            .mapToOneOrNull(dbContext)
            .flowOn(dbContext)
            .conflate()
            .map { dbEntity -> dbEntity?.toEntity(gson) }
    }

    override fun getEntities(): List<Entity> {
        return domainQueries.selectAll().executeAsList().map { dbEntity -> dbEntity.toEntity(gson) }
    }

    override fun getEntity(): Entity? {
        return domainQueries.selectAll().executeAsOneOrNull()?.toEntity(gson)
    }

    override fun delete(id: Long) = domainQueries.deleteDbEntity(id)

    override fun insert(entity: Entity) {
        CoroutineScope(dbContext).launch {
            domainQueries.insertDbEntity(entity.toJson(gson))
        }
    }
}

// todo save as json
fun Entity.toJson(gson: Gson): String {
    return gson.toJson(this)
}

fun DbEntity.toEntity(gson: Gson): Entity {
    return gson.fromJson(this.json, Entity::class.java)
}