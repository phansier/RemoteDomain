package ru.beryukhov.remote_domain.main

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import ru.beryukhov.common.model.Entity
import ru.beryukhov.remote_domain.R
import ru.beryukhov.remote_domain.domain.Post
import ru.beryukhov.remote_domain.domain.User
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            entity = it.getString(ENTITY_ARGUMENT, ENTITY_POST)
        }

        adapter = setupRecycler()

        setupButtons()

    }

    private fun setupButtons() {
        fab.setOnClickListener {
            when (entity){
                ENTITY_POST -> findNavController().navigate(R.id.action_main_to_post)
                ENTITY_USER -> findNavController().navigate(R.id.action_main_to_user)
            }

        }
    }

    override fun updateEntityUI(entity: Entity) {
        adapter.clearAll()
        when (this.entity){
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
    return this.data?.get("User")?.data?.entries?.map { it -> User(it.key, it.value!!) }
}

fun Entity.posts(): List<Post>? {
    return this.data?.get("Post")?.data?.entries?.map { it -> Post(it.key, it.value!!) }
}
