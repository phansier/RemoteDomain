package ru.beryukhov.remote_domain

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.beryukhov.client_lib.db.Dao
import ru.beryukhov.client_lib.db.DatabaseImpl
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.User
import ru.beryukhov.remote_domain.db.UserDao
import ru.beryukhov.remote_domain.db.testDb
import ru.beryukhov.client_lib.http.HttpClientRepositoryImpl
import ru.beryukhov.common.model.Post
import ru.beryukhov.remote_domain.push.OkHttpPush
import ru.beryukhov.remote_domain.recycler.DomainListAdapter
import ru.beryukhov.remote_domain.recycler.UserItem


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var dbRepo: DatabaseImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupNetworkButton()
        setupDatabaseButton()
        setupSocketButton()
        setupCreateDbButton()
    }

    private fun setupNetworkButton() {
        val button: Button = findViewById(R.id.button_http)
        button.setOnClickListener {
            testHttp(::log)
        }
    }

    private fun setupDatabaseButton() {
        val button: Button = findViewById(R.id.button_db)
        button.setOnClickListener {
            testDb(this, ::log)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupSocketButton() {
        val broadcastChannel = BroadcastChannel<Any>(Channel.CONFLATED)

        val button: Button = findViewById(R.id.button_socket)
        val push = OkHttpPush()
        val gson = Gson()
        button.setOnClickListener {
            push.startReceive(socketUrl = SOCKET_URL, log = ::log) {
                try {
                    //{"method":"Create","entity":"Post"}
                    val apiRequest =
                        gson.fromJson<ApiRequest>(it.toString(), ApiRequest::class.java)
                    //todo change User and Post instanses by corresponding type flags
                    when (apiRequest.entity) {
                        "User" -> broadcastChannel.offer(User("", ""))
                        "Post" -> broadcastChannel.offer(Post("", "", ""))
                    }
                } catch (e: JsonSyntaxException) {
                    Log.i("MainActivity", "JsonSyntaxException $e")
                } catch (e: RuntimeException) {
                    Log.i("MainActivity", "RuntimeException $e")
                }
            }
        }

        val httpClientRepository =
            HttpClientRepositoryImpl(SERVER_URL, BuildConfig.DEBUG,::log)
        //val userDao = UserDao(context, log)
        //val dbRepo = DatabasePreferencesImpl().addDao(BackUser::class,userDao)

        GlobalScope.launch {
            broadcastChannel.consumeEach {
                log("event got $it")
                when (it) {
                    is User -> {/*http<User>->db*/
                        val result = httpClientRepository.clientApi.get("user") as Result<List<User>>//todo user to hashmap
                        if (result is Result.Success){
                            val users = result.value
                            val backUsersMap = users.associateBy({ it.id }, { it })
                            val userDao = dbRepo.getDao(User::class) as Dao<User>
                            val dbUserIds = userDao.getEntities().map { it.id }

                            for (dbUserId in dbUserIds){
                                if (! backUsersMap.contains(dbUserId)){
                                    //remove values from db that are not in backend
                                    userDao.delete(dbUserId)
                                }
                                else{
                                    //update values from db that are in backend
                                    userDao.insert(backUsersMap.getValue(dbUserId))
                                }
                            }
                            //update values from backend that are not in db
                            for (backUserId in backUsersMap.keys) {
                                if (!dbUserIds.contains(backUserId)) {
                                    userDao.insert(backUsersMap.getValue(backUserId))
                                }
                            }
                        }
                    }
                    is Post -> {/*http<Post>->db*/
                    }
                }
            }
        }
    }

    private fun setupCreateDbButton() {
        val button: Button = findViewById(R.id.button_create_db)
        val adapter = setupRecycler()


        dbRepo = DatabaseImpl().apply{addDao(User::class,
            UserDao(this@MainActivity, ::log)
        )}
        val userDao = dbRepo.getDao(User::class) as Dao<User>

        button.setOnClickListener {
            userDao.createTable()
            userDao.getEntitiesFlow().onEach {
                Log.d("DR_", "onEach")
                log(it.toString())

                withContext(Dispatchers.Main) {
                    adapter.clearAll()
                    adapter.add(it
                        .map { item -> UserItem(item) }
                    )
                }
            }.launchIn(CoroutineScope(Dispatchers.Default))


            //todo remove
            /*GlobalScope.launch {
                for (i in 1..8) {
                    Log.d("DR_", "emit $i")
                    delay(2000)
                    dbRepo.insertUser(
                        ru.beryukhov.remote_domain.User.Impl(
                            "user_id_$i",
                            "user_name_$i"
                        )
                    )
                }
            }*/
        }
    }

    private fun setupRecycler(): DomainListAdapter {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val adapter = DomainListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        return adapter
    }


    private suspend fun log(s: String) {
        Log.i("MainActivity", s)
        /*withContext(Dispatchers.Main) {
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "${textView.text}\n$s"
        }*/
    }

}

data class ApiRequest(val method: String, val entity: String)
