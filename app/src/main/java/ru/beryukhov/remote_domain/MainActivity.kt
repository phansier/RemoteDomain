package ru.beryukhov.remote_domain

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
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

        setupButtons()

        val remoteDomainClient = RemoteDomainClient(application)
        remoteDomainClient.firstInit()
        remoteDomainClient.getEntitiesFlow().onEach(::updateEntityUI)
            .launchIn(CoroutineScope(Dispatchers.Default))

        remoteDomainClient.init(SERVER_URL, SOCKET_URL, BuildConfig.DEBUG)
    }

    private fun setupButtons() {
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.addUser -> {
                    Snackbar.make(recycler_view, "Add user", Snackbar.LENGTH_LONG).show()
                    true
                }

                else -> false
            }
        }
        fab.setOnClickListener {
            Snackbar.make(recycler_view, "Add message", Snackbar.LENGTH_LONG).show()
        }
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
        val adapter = DomainListAdapter()
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        return adapter
    }

}

fun Entity.users(): List<User>? {
    return this.data?.get("User")?.data?.entries?.map { it -> User(it.key, it.value!!) }
}
