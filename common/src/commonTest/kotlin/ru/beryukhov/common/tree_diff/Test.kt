package ru.beryukhov.common.tree_diff

import ru.beryukhov.common.model.Entity
import kotlin.test.Test
import kotlin.test.assertEquals

class DiffTest {
    private val a_users = "User" to Entity(
        mapOf(
            "0" to Entity(leaf = "TestTestov0"),
            "1" to Entity(leaf = "TestTestov1")
        )
    )
    private val a_posts = "Post" to Entity(
        mapOf(
            "0" to Entity(
                mapOf(
                    "UserId" to Entity(leaf = "0"),
                    "Message" to Entity(leaf = "Hello world0")
                )
            ),
            "1" to Entity(
                mapOf(
                    "UserId" to Entity(leaf = "1"),
                    "Message" to Entity(leaf = "Hello world1")
                )
            )
        )
    )
    private val a = Entity(
        mapOf(
            a_users,
            a_posts
        )
    )
    private val a_users_add = "User" to Entity(
        mapOf(
            "0" to Entity(leaf = "TestTestov0"),
            "1" to Entity(leaf = "TestTestov1"),
            "2" to Entity(leaf = "TestTestov2")
        )
    )

    private val a_add_user = Entity(
        mapOf(
            a_users_add,
            a_posts
        )
    )

    private val a_users_change = "User" to Entity(
        mapOf(
            "0" to Entity(leaf = "TestTestov0"),
            "1" to Entity(leaf = "TestTestovChange")
        )
    )

    private val a_change_user = Entity(
        mapOf(
            a_users_change,
            a_posts
        )
    )

    private val `a_add_user-a` = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "2" to Entity(leaf = "TestTestov2")
                )
            )
        )
    )

    private val `a-a_add_user` = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "2" to null
                )
            )
        )
    )

    private val `a_change_user-a` = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "1" to Entity(leaf = "TestTestovChange")
                )
            )
        )
    )
    @Test
    fun testAddUser() {
        assertEquals(`a_add_user-a`, DiffImpl.get(a, a_add_user))
    }

    @Test
    fun testRemoveUser() {
        assertEquals(`a-a_add_user`, DiffImpl.get(a_add_user, a))
    }

    @Test
    fun testChangeUser() {
        assertEquals(`a_change_user-a`, DiffImpl.get(a, a_change_user))
    }

}