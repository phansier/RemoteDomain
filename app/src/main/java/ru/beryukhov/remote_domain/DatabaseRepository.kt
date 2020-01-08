package ru.beryukhov.remote_domain

import android.content.Context
import android.util.Log
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.*

/**
 * Created by Andrey Beryukhov
 */
@UseExperimental(
    InternalCoroutinesApi::class,
    ExperimentalCoroutinesApi::class,
    ObsoleteCoroutinesApi::class
)
fun testDb(context: Context, log: suspend (String) -> Unit) {
    val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "test.db")
    val database = Database(driver)
    val userQueries: UserQueries = database.userQueries
    Log.d("DR_", "1")
    val dbContext = newSingleThreadContext("DB")
    CoroutineScope(dbContext).launch {

        Log.d("DR_", "2")
        userQueries.createUserTable()
        Log.d("DR_", "3")
        userQueries.selectAll().addListener(object : Query.Listener {
            override fun queryResultsChanged() {
                Log.d("DR_", "onEach")
                val result = userQueries.selectAll().executeAsList().toString()
                CoroutineScope(dbContext).launch {
                    log(result)
                }
            }
        })

        //userQueries.selectAll().asFlow().flowOn(Dispatchers.IO).collect { value -> log(value.executeAsList().toString()) }
        /*userQueries.selectAll().asFlow().onEach { value ->
            run {

                Log.d("DR_", "onEach")
                //log(value.executeAsList().toString())
                val result = value.executeAsList().toString()
                CoroutineScope(dbContext).launch {
                    log(result)
                }
            }
        }*/

        Log.d("DR_", "emit 1")
        userQueries.insertUser(User.Impl("user_id0", "user_name"))
        Log.d("DR_", "emit 2")
        userQueries.insertUser(User.Impl("user_id1", "user_name"))
        Log.d("DR_", "emit 3")
        userQueries.insertUser(User.Impl("user_id2", "user_name"))
        Log.d("DR_", "emit 4")
        userQueries.insertUser(User.Impl("user_id3", "user_name"))

        //log(userQueries.selectAll().executeAsList().toString())

        userQueries.deleteUserTable()
    }
}