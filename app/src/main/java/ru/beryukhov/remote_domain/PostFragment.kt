package ru.beryukhov.remote_domain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.post_fragment.*
import kotlinx.android.synthetic.main.user_fragment.*
import kotlinx.android.synthetic.main.user_fragment.button
import kotlinx.android.synthetic.main.user_fragment.textField
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.remote_domain.domain.Post
import ru.beryukhov.remote_domain.domain.User

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PostFragment : Fragment() {

    private val remoteDomainClient: RemoteDomainClient by lazy {
        (requireActivity().application as TheApplication).theInteractor.remoteDomainClient
    }

    private val args: PostFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.post_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val post = args.post
        if (post != null) {
            post_id.text = post.id
            userTextField.editText?.setText(post.userId)
            textField.editText?.setText(post.message)
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                remoteDomainClient.pushChanges(
                    post.deleteDiff
                )
                findNavController().popBackStack()
            }
        }

        val items = listOf("Material", "Design", "Components", "Android") //todo real users
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        (userTextField.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        textField.editText?.doOnTextChanged { inputText, _, _, _ ->
            val text = textField.editText?.text.toString()
            if (text.isNotEmpty()) {
                button.visibility = View.VISIBLE
                button.text = if (post != null) "Update" else "Create"
                button.setOnClickListener {
                    if (post == null) {
                        remoteDomainClient.pushChanges(
                            Post("-1", "-1", text).createDiff//todo think about unique id
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
}