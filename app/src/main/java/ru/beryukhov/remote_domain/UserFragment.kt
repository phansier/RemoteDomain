package ru.beryukhov.remote_domain

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.remote_domain.domain.User
import ru.beryukhov.remote_domain.main.posts
import ru.beryukhov.remote_domain.main.users
import ru.beryukhov.remote_domain.recycler.DomainListAdapter
import ru.beryukhov.remote_domain.recycler.PostItem

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class UserFragment : Fragment(R.layout.user_fragment) {

    private val remoteDomainClient: RemoteDomainClient by lazy {
        (requireActivity().application as TheApplication).theInteractor.remoteDomainClient
    }

    private val args: UserFragmentArgs by navArgs()

    private lateinit var adapter: DomainListAdapter

    private lateinit var user_id: TextView
    private lateinit var userTextField: TextInputLayout
    private lateinit var textField: TextInputLayout
    private lateinit var button: Button
    private lateinit var deleteButton: Button
    private lateinit var recycler_view: RecyclerView

    private fun View.findViews() {
        user_id = findViewById(R.id.user_id)
        userTextField = findViewById(R.id.userTextField)
        textField = findViewById(R.id.textField)
        button = findViewById(R.id.button)
        deleteButton = findViewById(R.id.deleteButton)
        recycler_view = findViewById(R.id.recycler_view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViews()
        val user = args.user

        if (user != null) {
            user_id.text = user.id
            textField.editText?.setText(user.userName)
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                remoteDomainClient.pushChanges(
                    user.deleteDiff
                )
                findNavController().popBackStack()
            }
        }

        textField.editText?.doOnTextChanged { inputText, _, _, _ ->
            val text = textField.editText?.text.toString()
            if (text.isNotEmpty()) {
                button.visibility = View.VISIBLE
                button.text = if (user != null) "Update" else "Create"
                button.setOnClickListener {
                    if (user == null) {
                        remoteDomainClient.pushChanges(
                            User(
                                id = remoteDomainClient.getNewId(),
                                userName = text
                            ).createDiff
                        )
                    } else {
                        remoteDomainClient.pushChanges(
                            user.copy(userName = text).updateDiff
                        )
                    }
                    findNavController().popBackStack()
                }
            } else {
                button.visibility = View.GONE
            }
        }

        adapter = setupRecycler()
    }

    private fun setupRecycler(): DomainListAdapter {
        val adapter = DomainListAdapter()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = adapter

        val user = args.user
        if (user != null) {
            val entity = remoteDomainClient.getEntity()
            val users = entity.users()
            adapter.add(entity.posts()
                ?.filter { post -> post.userId == user.id }
                ?.map { item -> PostItem(item, users?.find { it.id == item.userId }) }
            )
        }

        return adapter
    }
}