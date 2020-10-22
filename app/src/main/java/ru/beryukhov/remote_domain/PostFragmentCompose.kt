package ru.beryukhov.remote_domain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.material.Button
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.ui.tooling.preview.Preview


class PostFragmentCompose : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(
        requireContext(),
        attrs = null,
        defStyleAttr = 0
    ).apply { setContent { PostPage() } }
}

@Composable
fun PostPage(post_id: String = "0") {
    ConstraintLayout() {
        val (postIdRef, userTextFieldRef, textFiledRef, buttonRef, deleteButtonRef) = createRefs()
        Text(text = post_id, modifier = Modifier.constrainAs(postIdRef) {
            top.linkTo(parent.top, margin = 24.dp)
            start.linkTo(parent.start, margin = 24.dp)
            end.linkTo(parent.end, margin = 24.dp)
        })
        var userName: String by remember { mutableStateOf("") }
        TextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("UserName") },
            modifier = Modifier.constrainAs(userTextFieldRef) {
                top.linkTo(postIdRef.bottom, margin = 4.dp)
                start.linkTo(parent.start, margin = 24.dp)
                end.linkTo(parent.end, margin = 24.dp)
            }
        )
        var message: String by remember { mutableStateOf("") }
        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.constrainAs(textFiledRef) {
                top.linkTo(userTextFieldRef.bottom, margin = 4.dp)
                start.linkTo(parent.start, margin = 24.dp)
                end.linkTo(parent.end, margin = 24.dp)
            }
        )
        Button(onClick = {},
            modifier = Modifier.constrainAs(buttonRef) {
                top.linkTo(textFiledRef.bottom, margin = 16.dp)
                start.linkTo(parent.start, margin = 24.dp)
                end.linkTo(deleteButtonRef.start, margin = 8.dp)
            }) {
            Text("Save")
        }
        Button(onClick = {},
            backgroundColor = Color.Red,
            contentColor = Color.White,
            modifier = Modifier.constrainAs(deleteButtonRef) {
                top.linkTo(textFiledRef.bottom, margin = 16.dp)
                start.linkTo(buttonRef.end, margin = 8.dp)
                end.linkTo(parent.end, margin = 24.dp)
            }) {
            Text("Delete")
        }
    }
}

@Preview
@Composable
fun PostPagePreview() = PostPage()