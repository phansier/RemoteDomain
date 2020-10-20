package ru.beryukhov.remote_domain

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.ui.tooling.preview.Preview
import ru.beryukhov.remote_domain.domain.Post
import ru.beryukhov.remote_domain.domain.User
import ru.beryukhov.remote_domain.recycler.PostItem
import androidx.compose.ui.unit.dp


@Composable
fun PostList(items: List<PostItem>) {
    LazyColumnFor(
        items = items,
        itemContent = { item ->
            PostItem(item)
        },
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
fun PostItem(postItem: PostItem) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top=4.dp)
    ) {
        val postTitle = (
                if (BuildConfig.SHOW_ENTITY_ID)
                    "${postItem.user?.id} : "
                else ""
                ) +
                postItem.user?.userName
        val postMessage = (
                if (BuildConfig.SHOW_ENTITY_ID)
                    "${postItem.post.id} : "
                else ""
                ) +
                postItem.post.message
        ConstraintLayout() {
            val (postTitleRef, postMessageRef) = createRefs()
            Text(text = postTitle, modifier = Modifier.constrainAs(postTitleRef) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
            }.fillMaxWidth())
            Text(
                text = postMessage,
                modifier = Modifier.fillMaxWidth().constrainAs(postMessageRef) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    top.linkTo(postTitleRef.bottom)

                })
        }
    }
}

val testPostItem =
    PostItem(
        Post("id0", "userId", "postMessage"),
        User("id1", "userName")
    )


@Preview
@Composable
fun PostItemPreview() {
    PostItem(testPostItem)
}

@Preview
@Composable
fun PostListPreview() {
    PostList(listOf(testPostItem, testPostItem))
}

