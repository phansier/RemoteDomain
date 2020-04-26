package ru.beryukhov.remote_domain

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.common.model.Entity
import ru.beryukhov.remote_domain.domain.User
import ru.beryukhov.remote_domain.recycler.DomainListAdapter
import ru.beryukhov.remote_domain.recycler.UserItem


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var adapter: DomainListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = setupRecycler()

        val remoteDomainClient = RemoteDomainClient(application)
        remoteDomainClient.firstInit()
        remoteDomainClient.getEntitiesFlow().onEach(::updateEntityUI)
            .launchIn(CoroutineScope(Dispatchers.Default))

        remoteDomainClient.init(SERVER_URL, SOCKET_URL, BuildConfig.DEBUG)
    }

    //todo replace by single entity
    private suspend fun updateEntityUI(entities: List<Entity>) {
        Log.d("MainActivity", "onEach: [$entities]")

        withContext(Dispatchers.Main) {
            adapter.clearAll()
            adapter.add(entities.lastOrNull()?.users()
                ?.map { item -> UserItem(item) }
            )
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

fun Entity.users(): List<User>? {
    return this.data?.get("User")?.data?.entries?.map { it -> User(it.key, it.value!!) }
}
