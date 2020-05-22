package ru.beryukhov.common.tree_diff

import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.PrivacyMode
import kotlin.test.Test
import kotlin.test.assertEquals

class PrivacyDiffTest {

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

    private val a_users_add_privacy = "User" to Entity(
        mapOf(
            "0" to Entity(
                leaf = "TestTestov0",
                creatorClientId = "42",
                privacyMode = PrivacyMode.PRIVATE
            ),
            "1" to Entity(leaf = "TestTestov1")
        )
    )

    private val a_users_add_privacy1 = "User" to Entity(
        mapOf(
            "0" to Entity(leaf = "TestTestov0"),
            "1" to Entity(
                leaf = "TestTestov1",
                creatorClientId = "13",
                privacyMode = PrivacyMode.READ
            )
        )
    )

    private val a_add_privacy = Entity(
        mapOf(
            a_users_add_privacy,
            a_posts
        )
    )

    private val a_add_privacy1 = Entity(
        mapOf(
            a_users_add_privacy1,
            a_posts
        )
    )

    private val `a_add_privacy-a` = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to Entity(
                        leaf = "TestTestov0",
                        creatorClientId = "42",
                        privacyMode = PrivacyMode.PRIVATE
                    )
                )
            )
        )
    )

    private val `a-a_add_privacy` = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to Entity(
                        leaf = "TestTestov0",
                        creatorClientId = null,
                        privacyMode = PrivacyMode.UPDATE
                    ) // same as Entity(leaf = "TestTestov0")
                )
            )
        )
    )

    private val a_users_change_privacy = "User" to Entity(
        mapOf(
            "0" to Entity(
                leaf = "TestTestov0",
                creatorClientId = "42",
                privacyMode = PrivacyMode.READ
            ),
            "1" to Entity(leaf = "TestTestov1")
        )
    )

    private val a_users_change_clientId = "User" to Entity(
        mapOf(
            "0" to Entity(
                leaf = "TestTestov0",
                creatorClientId = "13",
                privacyMode = PrivacyMode.PRIVATE
            ),
            "1" to Entity(leaf = "TestTestov1")
        )
    )

    private val a_change_privacy = Entity(
        mapOf(
            a_users_change_privacy,
            a_posts
        )
    )

    private val a_change_clientId = Entity(
        mapOf(
            a_users_change_clientId,
            a_posts
        )
    )

    private val `a_change_privacy-a_add_privacy` = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to Entity(
                        leaf = "TestTestov0",
                        creatorClientId = "42",
                        privacyMode = PrivacyMode.READ
                    )
                )
            )
        )
    )

    private val `a_change_clientId-a_add_privacy` = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to Entity(
                        leaf = "TestTestov0",
                        creatorClientId = "13",
                        privacyMode = PrivacyMode.PRIVATE
                    )
                )
            )
        )
    )

    @Test
    fun testAddPrivacy() {
        assertEquals(`a_add_privacy-a`, DiffImpl.get(a, a_add_privacy))
    }

    @Test
    fun testRemovePrivacy() {
        assertEquals(`a-a_add_privacy`, DiffImpl.get(a_add_privacy, a))
    }

    @Test
    fun testChangePrivacy() {
        assertEquals(
            `a_change_privacy-a_add_privacy`,
            DiffImpl.get(a_add_privacy, a_change_privacy)
        )
    }

    @Test
    fun testChangeClientId() {
        assertEquals(
            `a_change_clientId-a_add_privacy`,
            DiffImpl.get(a_add_privacy, a_change_clientId)
        )
    }

    @Test
    fun testApplyAddPrivacy() {
        assertEquals(a_add_privacy, DiffImpl.apply(a, `a_add_privacy-a`))
    }

    @Test
    fun testApplyRemovePrivacy() {
        assertEquals(a, DiffImpl.apply(a_add_privacy, `a-a_add_privacy`))
    }

    @Test
    fun testApplyChangePrivacy() {
        assertEquals(
            a_change_privacy,
            DiffImpl.apply(a_add_privacy, `a_change_privacy-a_add_privacy`)
        )
    }

    @Test
    fun testApplyChangeClientId() {
        assertEquals(
            a_change_clientId,
            DiffImpl.apply(a_add_privacy, `a_change_clientId-a_add_privacy`)
        )
    }

    @Test
    fun complexDiffing() {
        val diff1 = DiffImpl.get(a, a_add_privacy)
        val diff2 = DiffImpl.get(a, a_add_privacy1)
        val diff = DiffImpl.apply(diff1!!, diff2)
        assertEquals(DiffImpl.apply(a_add_privacy, diff2), DiffImpl.apply(a, diff))
    }
}