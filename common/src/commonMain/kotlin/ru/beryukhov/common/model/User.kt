package ru.beryukhov.common.model

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

@Serializable
data class User(val id: String, val userName: String)

@Serializable
class UserList(
    val items: List<User>
) {

    @Serializer(UserList::class)
    companion object : KSerializer<UserList> {

        override val descriptor = StringDescriptor.withName("UserList")

        override fun serialize(encoder: Encoder, obj: UserList) {
            User.serializer().list.serialize(encoder, obj.items)
        }

        override fun deserialize(decoder: Decoder): UserList {
            return UserList(User.serializer().list.deserialize(decoder))
        }
    }
}