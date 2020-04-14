package ru.beryukhov.remote_domain.db

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.beryukhov.client_lib.db.Dao
import ru.beryukhov.common.model.User
import ru.beryukhov.client_lib.db.DatabaseImpl

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun testDb(context: Context, log: suspend (String) -> Unit) {
    val dbRepo = DatabaseImpl().apply{addDao(User::class,
        UserDao(context, log)
    )}
    val userDao = dbRepo.getDao(User::class) as Dao<User>

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
    userDao.insert(User("user_id0", "user_name"))
    Log.d("DR_", "emit 2")
    userDao.insert(User("user_id1", "user_name"))
    Log.d("DR_", "emit 3")
    userDao.insert(User("user_id2", "user_name"))
    Log.d("DR_", "emit 4")
    userDao.insert(User("user_id3", "user_name"))
}
