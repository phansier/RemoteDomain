package ru.beryukhov.remote_domain.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.main_fragment.*
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

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainFragment : MvpAppCompatFragment(R.layout.main_fragment),
    MainView {

    private val presenter by moxyPresenter {
        MainPresenter(requireActivity().application)
    }

    private lateinit var adapter: DomainListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = setupRecycler()

        setupButtons()
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

    override fun updateEntityUI(entity: Entity) {
        adapter.clearAll()
        val users = entity.users()
        adapter.add(entity.posts()
            ?.map { item -> PostItem(item, users?.find { it.id == item.userId }) }
        )
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
