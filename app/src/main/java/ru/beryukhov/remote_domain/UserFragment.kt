package ru.beryukhov.remote_domain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.user_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.remote_domain.domain.User

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class UserFragment : Fragment() {

    private val remoteDomainClient: RemoteDomainClient by lazy {
        (requireActivity().application as TheApplication).theInteractor.remoteDomainClient
    }

    private val args: UserFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.user_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                            User(id = remoteDomainClient.getNewId(), userName = text).createDiff
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
    }
}