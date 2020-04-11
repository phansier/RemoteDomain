package ru.beryukhov.remote_domain

import android.content.Context
import android.util.Log
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.beryukhov.client_lib.db.Dao
import ru.beryukhov.common.model.Entity
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * Created by Andrey Beryukhov
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun testDb(context: Context, log: suspend (String) -> Unit) {
    val userDao = UserDao(context, log)
    val dbRepo = DatabasePreferencesImpl().apply { addDao(BackUser::class,userDao)}

    userDao.createTable()
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
        userDao.getEntitiesFlow().collect {
            Log.d("DR_", "onEach")
            log(it.toString())
        }
    }

    //Thread.sleep(1000)

    Log.d("DR_", "emit 1")
    userDao.insert(BackUser("user_id0", "user_name"))
    Log.d("DR_", "emit 2")
    userDao.insert(BackUser("user_id1", "user_name"))
    Log.d("DR_", "emit 3")
    userDao.insert(BackUser("user_id2", "user_name"))
    Log.d("DR_", "emit 4")
    userDao.insert(BackUser("user_id3", "user_name"))
}

interface DatabasePreferences{
    fun addDao(entity: KClass<out Entity>, dao: Dao<out Entity>)
    fun getDao(entity: KClass<out Entity>): Dao<out Entity>?
}
class DatabasePreferencesImpl():DatabasePreferences{
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

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class UserDao(context: Context, private val log: suspend (String) -> Unit): Dao<BackUser> {
    private val userQueries: UserQueries
    private val dbContext: CoroutineContext

    init {
        val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "test.db")
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

    override fun getEntitiesFlow(): Flow<List<BackUser>> {
        return userQueries.selectAll().asFlow()
            .mapToList(dbContext)
            .flowOn(dbContext)
            .conflate()
            .map{list->list.map(User::map)}
    }

    override fun getEntities(): List<BackUser> {
        return userQueries.selectAll().executeAsList().map(User::map)
    }

    override fun delete(id: String) = userQueries.deleteUser(id)

    override fun insert(entity: BackUser) {
        CoroutineScope(dbContext).launch {
            userQueries.insertUser(entity.map())
        }
    }
}