package ru.beryukhov.remote_domain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonConstants.defaultButtonColors
import androidx.compose.material.ContainerAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.ui.tooling.preview.Preview
import ru.beryukhov.client_lib.RemoteDomainClient


class PostFragmentCompose : Fragment() {
    private val remoteDomainClient: RemoteDomainClient by lazy {
        (requireActivity().application as TheApplication).theInteractor.remoteDomainClient
    }

    private val args: PostFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val post = args.post
        val users = remoteDomainClient.getEntity().users()!!
        //todo add users list to edittext for autocomplete

        val postPageData = if (post != null) {
            val user = users.find { it.id == post.userId }
            val userString = if (BuildConfig.SHOW_ENTITY_ID)
                user.toString()
            else
                user?.userName
            val deleteButtonOnClickListener: () -> Unit = {
                remoteDomainClient.pushChanges(
                    post.deleteDiff
                )
                findNavController().popBackStack()
            }
            PostPageData(
                postIdText = post.id,
                userText = userString ?: "",
                messageText = post.message,
                deleteButtonOnClick = deleteButtonOnClickListener
            )
        } else PostPageData()

        return ComposeView(
            requireContext(),
            attrs = null,
            defStyleAttr = 0
        ).apply { setContent { PostPage(postPageData) } }
    }
}

data class PostPageData(
    val postIdText: String = "",
    val userText: String = "",
    val messageText: String = "",
    val deleteButtonOnClick: () -> Unit = {}
)

@Composable
fun PostPage(postPageData: PostPageData = PostPageData()) {

    ConstraintLayout(modifier = Modifier.padding(24.dp)) {
        val (postIdRef, userTextFieldRef, textFiledRef, buttonRef, deleteButtonRef) = createRefs()
        Text(text = postPageData.postIdText, modifier = Modifier.constrainAs(postIdRef) {
            top.linkTo(parent.top, margin = 0.dp)
            start.linkTo(parent.start, margin = 0.dp)
        })
        var userName: String by remember { mutableStateOf(postPageData.userText) }
        TextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("UserName") },
            backgroundColor = MaterialTheme.colors.background.copy(alpha = ContainerAlpha),
            modifier = Modifier.constrainAs(userTextFieldRef) {
                top.linkTo(postIdRef.bottom, margin = 4.dp)
                start.linkTo(parent.start, margin = 0.dp)
                end.linkTo(parent.end, margin = 0.dp)
                width = Dimension.fillToConstraints
            }
        )
        var message: String by remember { mutableStateOf(postPageData.messageText) }
        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            backgroundColor = MaterialTheme.colors.background.copy(alpha = ContainerAlpha),
            modifier = Modifier.constrainAs(textFiledRef) {
                top.linkTo(userTextFieldRef.bottom, margin = 4.dp)
                start.linkTo(parent.start, margin = 0.dp)
                end.linkTo(parent.end, margin = 0.dp)
                width = Dimension.fillToConstraints
            }
        )
        var saveButtonVisible: Boolean by remember { mutableStateOf(false) }
        if (saveButtonVisible) {
            Button(onClick = {},
                modifier = Modifier.constrainAs(buttonRef) {
                    top.linkTo(textFiledRef.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 8.dp)
                    end.linkTo(deleteButtonRef.start, margin = 8.dp)
                }) {
                Text("Save")
            }
        }
        var deleteButtonVisible: Boolean by remember { mutableStateOf(true) }
        if (deleteButtonVisible) {
            Button(onClick = postPageData.deleteButtonOnClick,
                colors = defaultButtonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                ),
                modifier = Modifier.constrainAs(deleteButtonRef) {
                    top.linkTo(textFiledRef.bottom, margin = 16.dp)
                    start.linkTo(buttonRef.end, margin = 8.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                }) {
                Text("Delete")
            }
        }
    }
}

@Preview
@Composable
fun PostPagePreview() = PostPage()