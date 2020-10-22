package ru.beryukhov.remote_domain

import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.remote_domain.domain.Post

class PostFragment : Fragment(R.layout.post_fragment) {

    private val remoteDomainClient: RemoteDomainClient by lazy {
        (requireActivity().application as TheApplication).theInteractor.remoteDomainClient
    }

    private val args: PostFragmentArgs by navArgs()

    private lateinit var post_id: TextView
    private lateinit var userTextField: TextInputLayout
    private lateinit var textField: TextInputLayout
    private lateinit var button: Button
    private lateinit var deleteButton: Button

    private fun View.findViews() {
        post_id = findViewById(R.id.post_id)
        userTextField = findViewById(R.id.userTextField)
        textField = findViewById(R.id.textField)
        button = findViewById(R.id.button)
        deleteButton = findViewById(R.id.deleteButton)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViews()
        val post = args.post
        val users = remoteDomainClient.getEntity().users()!!
        var currentUserId: String = users[0].id

        if (post != null) {
            post_id.text = post.id
            val user = users.find { it.id == post.userId }
            user?.id?.let { currentUserId = it }
            val userString =
                if (BuildConfig.SHOW_ENTITY_ID)
                    user.toString()
                else user?.userName
            userTextField.editText?.setText(userString)
            textField.editText?.setText(post.message)
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                remoteDomainClient.pushChanges(
                    post.deleteDiff
                )
                findNavController().popBackStack()
            }
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, users.map { it.userName })
        val userView = (userTextField.editText as? AutoCompleteTextView)
        userView?.setAdapter(adapter)
        userView?.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                currentUserId = users[position].id
                onDataChanged(post, currentUserId)
            }

        textField.editText?.doOnTextChanged { _, _, _, _ ->
            onDataChanged(post, currentUserId)
        }
    }

    private fun onDataChanged(post: Post?, userId: String) {
        val text = textField.editText?.text.toString()
        if (text.isNotEmpty()) {
            button.visibility = View.VISIBLE
            button.text = if (post != null) "Update" else "Create"
            button.setOnClickListener {
                if (post == null) {
                    remoteDomainClient.pushChanges(
                        Post(
                            id = remoteDomainClient.getNewId(),
                            userId = userId,
                            message = text
                        ).createDiff
                    )
                } else {
                    remoteDomainClient.pushChanges(
                        post.copy(message = text).updateDiff
                    )
                }
                findNavController().popBackStack()
            }
        } else {
            button.visibility = View.GONE
        }
    }
}