package ru.beryukhov.remote_domain.main

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import ru.beryukhov.common.model.Entity
import ru.beryukhov.remote_domain.R
import ru.beryukhov.remote_domain.domain.Post
import ru.beryukhov.remote_domain.domain.Post.Companion.POST
import ru.beryukhov.remote_domain.domain.User
import ru.beryukhov.remote_domain.domain.User.Companion.USER
import ru.beryukhov.remote_domain.recycler.DomainListAdapter
import ru.beryukhov.remote_domain.recycler.PostItem
import ru.beryukhov.remote_domain.recycler.UserItem

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class ListFragment : MvpAppCompatFragment(R.layout.list_fragment),
    MainView {

    private val presenter by moxyPresenter {
        MainPresenter(requireActivity().application)
    }

    private lateinit var adapter: DomainListAdapter
    private lateinit var entity: String

    private lateinit var fab: FloatingActionButton
    private lateinit var recycler_view: RecyclerView

    private fun View.findViews() {
        fab = findViewById(R.id.fab)
        recycler_view = findViewById(R.id.recycler_view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViews()
        arguments?.let {
            entity = it.getString(ENTITY_ARGUMENT, ENTITY_POST)
        }

        adapter = setupRecycler()

        setupButtons()

    }

    private fun setupButtons() {
        fab.setOnClickListener {
            when (entity) {
                ENTITY_POST -> findNavController().navigate(R.id.action_main_to_post)
                ENTITY_USER -> findNavController().navigate(R.id.action_main_to_user)
            }

        }
    }

    override fun updateEntityUI(entity: Entity) {
        adapter.clearAll()
        when (this.entity) {
            ENTITY_POST -> {
                val users = entity.users()
                adapter.add(entity.posts()
                    ?.map { item -> PostItem(item, users?.find { it.id == item.userId }) }
                )
            }
            ENTITY_USER -> {
                adapter.add(entity.users()
                    ?.map { item -> UserItem(item) }
                )
            }
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
    return this.data?.get(USER)?.data?.entries?.map { it -> User(it.key, it.value!!) }
}

fun Entity.posts(): List<Post>? {
    return this.data?.get(POST)?.data?.entries?.map { it -> Post(it.key, it.value!!) }
}
