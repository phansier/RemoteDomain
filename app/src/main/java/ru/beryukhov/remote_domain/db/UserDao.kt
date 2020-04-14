package ru.beryukhov.remote_domain.db

import android.content.Context
import android.util.Log
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.beryukhov.common.model.User
import ru.beryukhov.client_lib.db.Dao
import ru.beryukhov.remote_domain.*
import kotlin.coroutines.CoroutineContext

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class UserDao(context: Context, private val log: suspend (String) -> Unit):
    Dao<User> {
    private val userQueries: UserQueries
    private val dbContext: CoroutineContext

    init {
        val driver: SqlDriver =
            AndroidSqliteDriver(
                Database.Schema,
                context,
                "test.db"
            )
        val database = Database(driver)
        userQueries = database.userQueries
        Log.d("DR_", "Databaserepository Init")
        dbContext = newSingleThreadContext("DB")
    }

    override fun createTable() {
        CoroutineScope(dbContext).launch {
            log("DatabaseRepository:createDb() start")
            userQueries.deleteUserTable()
            userQueries.createUserTable()
            log("DatabaseRepository:createDb() created")
        }
    }

    override fun deleteTable() {
        CoroutineScope(dbContext).launch {
            log("DatabaseRepository:deleteDb() start")
            userQueries.deleteUserTable()
            log("DatabaseRepository:deleteDb() deleted")
        }
    }

    override fun getEntitiesFlow(): Flow<List<User>> {
        return userQueries.selectAll().asFlow()
            .mapToList(dbContext)
            .flowOn(dbContext)
            .conflate()
            .map{list->list.map(DbUser::map)}
    }

    override fun getEntities(): List<User> {
        return userQueries.selectAll().executeAsList().map(DbUser::map)
    }

    override fun delete(id: String) = userQueries.deleteUser(id)

    override fun insert(entity: User) {
        CoroutineScope(dbContext).launch {
            userQueries.insertUser(entity.map())
        }
    }
}