package ru.beryukhov.common.model

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

/**
 * Created by Andrey Beryukhov
 */
@Serializable
data class Post(val id: String, val userId: String, val message: String)

@Serializable
class PostList(
    val items: List<Post>
) {

    @Serializer(PostList::class)
    companion object : KSerializer<PostList> {

        override val descriptor = StringDescriptor.withName("PostList")

        override fun serialize(encoder: Encoder, obj: PostList) {
            Post.serializer().list.serialize(encoder, obj.items)
        }

        override fun deserialize(decoder: Decoder): PostList {
            return PostList(Post.serializer().list.deserialize(decoder))
        }
    }
}
