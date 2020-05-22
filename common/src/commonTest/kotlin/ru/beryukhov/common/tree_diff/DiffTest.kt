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

    private val a_add_comment = Entity(
        mapOf(
            a_users,
            a_posts,
            "Comment" to Entity(
                mapOf(
                    "0" to Entity(leaf = "SomeComment")
                )
            )
        )
    )

    private val `a_add_comment-a` = Entity(
        mapOf(
            "Comment" to Entity(
                mapOf(
                    "0" to Entity(leaf = "SomeComment")
                )
            )
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

    private val a_users_null_leaf = "User" to Entity(
        mapOf(
            "0" to Entity(leaf = null),
            "1" to Entity(leaf = "TestTestov1")
        )
    )

    private val `a+a_null_leaf_diff` = Entity(
        mapOf(
            a_users_null_leaf,
            a_posts
        )
    )

    private val a_posts_change = "Post" to Entity(
        mapOf(
            "2" to Entity(
                mapOf(
                    "UserId" to Entity(leaf = "0"),
                    "Message" to Entity(leaf = "Hello world0")
                )
            ),
            "1" to Entity(
                mapOf(
                    "UserId" to Entity(leaf = "1"),
                    "Message" to Entity(leaf = "Hello world2")
                )
            )
        )
    )

    private val a_change_post = Entity(
        mapOf(
            a_users,
            a_posts_change
        )
    )

    private val `a_change_post-a` = Entity(
        mapOf(
            "Post" to Entity(
                mapOf(
                    "0" to null,
                    "1" to Entity(
                        mapOf(
                            "Message" to Entity(leaf = "Hello world2")
                        )
                    ),
                    "2" to Entity(
                        mapOf(
                            "UserId" to Entity(leaf = "0"),
                            "Message" to Entity(leaf = "Hello world0")
                        )
                    )
                )
            )
        )
    )

    private val a_null_leaf_diff = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to Entity(leaf = null)
                )
            )
        )
    )

    @Test
    fun testAddUser() {
        assertEquals(`a_add_user-a`, DiffImpl.get(a, a_add_user))
    }

    @Test
    fun testAddNewEntity() {
        assertEquals(`a_add_comment-a`, DiffImpl.get(a, a_add_comment))
    }

    @Test
    fun testRemoveUser() {
        assertEquals(`a-a_add_user`, DiffImpl.get(a_add_user, a))
    }

    @Test
    fun testChangeUser() {
        assertEquals(`a_change_user-a`, DiffImpl.get(a, a_change_user))
    }

    @Test
    fun testApplyAddUser() {
        assertEquals(a_add_user, DiffImpl.apply(a, `a_add_user-a`))
    }

    @Test
    fun testApplyAddNewEntity() {
        assertEquals(a_add_comment, DiffImpl.apply(a, `a_add_comment-a`))
    }

    @Test
    fun testApplyRemoveUser() {
        assertEquals(a, DiffImpl.apply(a_add_user, `a-a_add_user`))
    }

    @Test
    fun testApplyChangeUser() {
        assertEquals(a_change_user, DiffImpl.apply(a, `a_change_user-a`))
    }

    @Test
    fun testChangePost() {
        assertEquals(`a_change_post-a`, DiffImpl.get(a, a_change_post))
    }

    @Test
    fun complexDiffing() {
        val diff1 = DiffImpl.get(a, a_add_user)
        val diff2 = DiffImpl.get(a, a_add_comment)
        val diff = DiffImpl.apply(diff1!!, diff2)
        assertEquals(DiffImpl.apply(a_add_user, diff2), DiffImpl.apply(a, diff))
    }

    @Test
    fun emtptyApplyDiff() {
        val empty = Entity()
        val diff = `a_add_user-a`
        assertEquals(diff, DiffImpl.apply(empty, diff))
    }

    @Test
    fun testApplyRemoveLeaf() {
        assertEquals(`a+a_null_leaf_diff`, DiffImpl.apply(a, a_null_leaf_diff))
    }

}