package ru.beryukhov.remote_domain

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Andrey Beryukhov
 */
fun testDb(context: Context, log: suspend (String) -> Unit) {
    val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "test.db")
    val database = Database(driver)
    val userQueries: UserQueries = database.userQueries

    GlobalScope.launch {
        userQueries.createUserTable()

        userQueries.insertUser(User.Impl("user_id0", "user_name"))
        userQueries.insertUser(User.Impl("user_id1", "user_name"))
        userQueries.insertUser(User.Impl("user_id2", "user_name"))
        userQueries.insertUser(User.Impl("user_id3", "user_name"))

        log(userQueries.selectAll().executeAsList().toString())

        userQueries.deleteUserTable()
    }
}