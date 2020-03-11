package ru.beryukhov.remote_domain

import android.content.Context
import android.util.Log
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by Andrey Beryukhov
 */
@UseExperimental(
    InternalCoroutinesApi::class,
    ExperimentalCoroutinesApi::class,
    ObsoleteCoroutinesApi::class
)
fun testDb(context: Context, log: suspend (String) -> Unit) {
    val dbRepo = DatabaseRepository(context, log)
    dbRepo.createDb()
    /*userQueries.selectAll().addListener(object : Query.Listener {
        override fun queryResultsChanged() {
            Log.d("DR_", "onEach")
            val result = userQueries.selectAll().executeAsList().toString()
            CoroutineScope(dbContext).launch {
                log(result)
            }
        }
    })*/
    CoroutineScope(Dispatchers.Default).launch {
        dbRepo.getUserFlow().collect {
            Log.d("DR_", "onEach")
            log(it.toString())
        }
    }

    //Thread.sleep(1000)

    Log.d("DR_", "emit 1")
    dbRepo.insertUser(User.Impl("user_id0", "user_name"))
    Log.d("DR_", "emit 2")
    dbRepo.insertUser(User.Impl("user_id1", "user_name"))
    Log.d("DR_", "emit 3")
    dbRepo.insertUser(User.Impl("user_id2", "user_name"))
    Log.d("DR_", "emit 4")
    dbRepo.insertUser(User.Impl("user_id3", "user_name"))
}

@UseExperimental(
    InternalCoroutinesApi::class,
    ExperimentalCoroutinesApi::class,
    ObsoleteCoroutinesApi::class
)
class DatabaseRepository(context: Context, private val log: suspend (String) -> Unit) {
    private val userQueries: UserQueries
    private val dbContext: CoroutineContext

    init {
        val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "test.db")
        val database = Database(driver)
        userQueries = database.userQueries
        Log.d("DR_", "Databaserepository Init")
        dbContext = newSingleThreadContext("DB")
    }

    fun createDb() {
        CoroutineScope(dbContext).launch {
            log("DatabaseRepository:createDb() start")
            userQueries.deleteUserTable()
            userQueries.createUserTable()
            log("DatabaseRepository:createDb() created")
        }
    }

    fun deleteDb() {
        CoroutineScope(dbContext).launch {
            log("DatabaseRepository:deleteDb() start")
            userQueries.deleteUserTable()
            log("DatabaseRepository:deleteDb() deleted")
        }
    }

    fun getUserFlow(): Flow<List<User>> {
        return userQueries.selectAll().asFlow()
            .buffer()
            .mapLatest { it.executeAsList() }
            .flowOn(Dispatchers.Default)

    }

    fun insertUser(user: User) {
        CoroutineScope(dbContext).launch {
            userQueries.insertUser(user)
        }
    }
}