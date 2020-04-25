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
import ru.beryukhov.client_lib.db.DaoStorageImpl
import ru.beryukhov.client_lib.db.EntityDao
import ru.beryukhov.client_lib.http.HttpClientRepositoryImpl
import ru.beryukhov.client_lib.push.OkHttpPush
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Success
import ru.beryukhov.remote_domain.domain.User
import ru.beryukhov.remote_domain.recycler.DomainListAdapter
import ru.beryukhov.remote_domain.recycler.UserItem


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var dbRepo: DaoStorageImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setupNetworkButton()
        //setupDatabaseButton()
        dbRepo = DaoStorageImpl().apply {
            addDao(
                Entity::class,
                EntityDao(this@MainActivity)
            )
        }
        setupSocketButton()
        setupCreateDbButton()
    }

    @SuppressLint("DefaultLocale")
    private fun setupSocketButton() {
        val broadcastChannel = BroadcastChannel<Any>(Channel.CONFLATED)

        val button: Button = findViewById(R.id.button_socket)
        val push = OkHttpPush()
        val gson = Gson()
        button.setOnClickListener {
            push.startReceive(socketUrl = SOCKET_URL) {
                try {
                    val apiRequest =
                        gson.fromJson<ApiRequest>(it.toString(), ApiRequest::class.java)
                    when (apiRequest.entity) {
                        "Entity" -> broadcastChannel.offer(Entity())
                    }
                } catch (e: JsonSyntaxException) {
                    Log.i("MainActivity", "JsonSyntaxException $e")
                } catch (e: RuntimeException) {
                    Log.i("MainActivity", "RuntimeException $e")
                }
            }
        }

        val httpClientRepository = HttpClientRepositoryImpl(SERVER_URL, BuildConfig.DEBUG)
        val entityDao = dbRepo.getDao(Entity::class) as Dao<Entity>

        GlobalScope.launch {
            broadcastChannel.consumeEach {
                Log.d("MainActivity", "event got $it")
                when (it) {
                    is Entity -> {
                        val result = httpClientRepository.clientApi.get("entity")
                        if (result is Success) {
                            //todo update diff instead of insert
                            result.value.forEach {
                                entityDao.insert(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupCreateDbButton() {
        val button: Button = findViewById(R.id.button_create_db)
        val adapter = setupRecycler()


        val entityDao = dbRepo.getDao(Entity::class) as Dao<Entity>

        button.setOnClickListener {
            entityDao.createTable()
            entityDao.getEntitiesFlow().onEach {
                //todo replace by single entity
                Log.d("MainActivity", "onEach: [$it]")

                withContext(Dispatchers.Main) {
                    adapter.clearAll()
                    adapter.add(it.lastOrNull()?.users()
                        ?.map { item -> UserItem(item) }
                    )
                }
            }.launchIn(CoroutineScope(Dispatchers.Default))

        }
    }

    private fun setupRecycler(): DomainListAdapter {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val adapter = DomainListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        return adapter
    }

}

data class ApiRequest(val method: String, val entity: String)

fun Entity.users(): List<User>? {
    return this.data?.get("User")?.data?.entries?.map { it -> User(it.key, it.value) }
}
