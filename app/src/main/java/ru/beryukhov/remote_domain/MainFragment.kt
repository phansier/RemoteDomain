package ru.beryukhov.remote_domain

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.common.model.Entity
import ru.beryukhov.remote_domain.domain.Post
import ru.beryukhov.remote_domain.domain.User
import ru.beryukhov.remote_domain.recycler.DomainListAdapter
import ru.beryukhov.remote_domain.recycler.PostItem

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainFragment : Fragment() {
    private lateinit var adapter: DomainListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = setupRecycler()

        setupButtons()

        val remoteDomainClient = RemoteDomainClient(requireActivity().application)
        remoteDomainClient.firstInit()
        remoteDomainClient.getEntitiesFlow().onEach(::updateEntityUI)
            .launchIn(CoroutineScope(Dispatchers.Default))

        remoteDomainClient.init(SERVER_URL, SOCKET_URL, BuildConfig.DEBUG)
    }

    private fun setupButtons() {
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.addUser -> {
                    findNavController().navigate(R.id.action_main_to_user)
                    true
                }

                else -> false
            }
        }
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_post)
        }
    }

    //todo replace by single entity
    private suspend fun updateEntityUI(entities: List<Entity>) {
        Log.d("MainActivity", "onEach: [$entities]")

        withContext(Dispatchers.Main) {
            adapter.clearAll()
            val users = entities.lastOrNull()?.users()
            adapter.add(entities.lastOrNull()?.posts()
                ?.map { item -> PostItem(item, users?.find { it.id == item.userId }) }
            )
        }
    }

    private fun setupRecycler(): DomainListAdapter {
        val adapter = DomainListAdapter()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = adapter
        return adapter
    }

}

fun Entity.users(): List<User>? {
    return this.data?.get("User")?.data?.entries?.map { it -> User(it.key, it.value!!) }
}

fun Entity.posts(): List<Post>? {
    return this.data?.get("Post")?.data?.entries?.map { it -> Post(it.key, it.value!!) }
}
