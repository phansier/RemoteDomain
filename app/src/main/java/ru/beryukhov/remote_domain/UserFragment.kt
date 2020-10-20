package ru.beryukhov.remote_domain

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.common.model.Entity
import ru.beryukhov.remote_domain.domain.Post
import ru.beryukhov.remote_domain.domain.User
import ru.beryukhov.remote_domain.recycler.PostItem

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class UserFragment : Fragment(R.layout.user_fragment) {

    private val remoteDomainClient: RemoteDomainClient by lazy {
        (requireActivity().application as TheApplication).theInteractor.remoteDomainClient
    }

    private val args: UserFragmentArgs by navArgs()

    private lateinit var user_id: TextView
    private lateinit var textField: TextInputLayout
    private lateinit var button: Button
    private lateinit var deleteButton: Button
    private lateinit var recycler_view: ComposeView

    private fun View.findViews() {
        user_id = findViewById(R.id.user_id)
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

        setupRecycler()
    }

    private fun setupRecycler() {
        val user = args.user
        if (user != null) {
            val entity = remoteDomainClient.getEntity()
            val users = entity.users()

            val items = entity.posts()
                ?.filter { post -> post.userId == user.id }
                ?.map { item -> PostItem(item, users?.find { it.id == item.userId }) }
            if (items!=null) {
                recycler_view.setContent { PostList(items) }
            }
        }
    }

}


fun Entity.users(): List<User>? {
    return this.data?.get(User.USER)?.data?.entries?.map { it -> User(it.key, it.value!!) }
}

fun Entity.posts(): List<Post>? {
    return this.data?.get(Post.POST)?.data?.entries?.map { it -> Post(it.key, it.value!!) }
}
