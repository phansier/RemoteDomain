package ru.beryukhov.remote_domain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        textField.editText?.doOnTextChanged { inputText, _, _, _ ->
            val text = textField.editText?.text.toString()
            if (text.isNotEmpty()) {
                button.visibility = View.VISIBLE
                button.text = "Create"
                button.setOnClickListener {
                    remoteDomainClient.pushChanges(
                        User("-1", text).createDiff//todo think about unique id
                    )
                    findNavController().popBackStack()
                }
            } else {
                button.visibility = View.GONE
            }
        }
    }
}