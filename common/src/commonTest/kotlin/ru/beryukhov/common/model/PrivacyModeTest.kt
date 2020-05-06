package ru.beryukhov.common.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PrivacyModeTest {
    private val creator1 = "creator1_id"
    private val creator2 = "creator2_id"

    private val a = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to Entity(
                        leaf = "TestTestov0",
                        creatorClientId = creator1,
                        privacyMode = PrivacyMode.PRIVATE
                    ),
                    "1" to Entity(leaf = "TestTestov1")
                ),
                creatorClientId = creator1
            ),
            "Post" to Entity(
                mapOf(
                    "0" to Entity(
                        mapOf(
                            "UserId" to Entity(
                                leaf = "0",
                                creatorClientId = creator1,
                                privacyMode = PrivacyMode.READ
                            ),
                            "Message" to Entity(leaf = "Hello world0")
                        )
                    ),
                    "1" to Entity(
                        mapOf(
                            "UserId" to Entity(
                                leaf = "1",
                                creatorClientId = creator1,
                                privacyMode = PrivacyMode.PRIVATE
                            ),
                            "Message" to Entity(leaf = "Hello world1")
                        ),
                        creatorClientId = creator1, privacyMode = PrivacyMode.READ
                    )
                )
            )
        )
    )

    private val a_filter_creator1 = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to Entity(
                        leaf = "TestTestov0",
                        creatorClientId = creator1,
                        privacyMode = PrivacyMode.PRIVATE
                    ),
                    "1" to Entity(leaf = "TestTestov1")
                ),
                creatorClientId = creator1
            ),
            "Post" to Entity(
                mapOf(
                    "0" to Entity(
                        mapOf(
                            "UserId" to Entity(
                                leaf = "0",
                                creatorClientId = creator1,
                                privacyMode = PrivacyMode.READ
                            ),
                            "Message" to Entity(leaf = "Hello world0")
                        )
                    ),
                    "1" to Entity(
                        mapOf(
                            "UserId" to Entity(
                                leaf = "1",
                                creatorClientId = creator1,
                                privacyMode = PrivacyMode.PRIVATE
                            ),
                            "Message" to Entity(leaf = "Hello world1")
                        ),
                        creatorClientId = creator1, privacyMode = PrivacyMode.READ
                    )
                )
            )
        )
    )

    private val a_filter_creator2 = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to null,
                    "1" to Entity(leaf = "TestTestov1")
                ),
                creatorClientId = creator1
            ),
            "Post" to Entity(
                mapOf(
                    "0" to Entity(
                        mapOf(
                            "UserId" to Entity(
                                leaf = "0",
                                creatorClientId = creator1,
                                privacyMode = PrivacyMode.READ
                            ),
                            "Message" to Entity(leaf = "Hello world0")
                        )
                    ),
                    "1" to Entity(
                        mapOf(
                            "UserId" to null,
                            "Message" to Entity(leaf = "Hello world1")
                        ),
                        creatorClientId = creator1, privacyMode = PrivacyMode.READ
                    )
                )
            )
        )
    )

    private val user0_diff = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "0" to Entity(leaf = "TestTestovChange")
                )
            )
        )
    )

    private val user1_diff = Entity(
        mapOf(
            "User" to Entity(
                mapOf(
                    "1" to Entity(leaf = "TestTestovChange")
                )
            )
        )
    )

    private val post0_diff = Entity(
        mapOf(
            "Post" to Entity(
                mapOf(
                    "0" to null
                )
            )
        )
    )
    private val post0_userid_diff = Entity(
        mapOf(
            "Post" to Entity(
                mapOf(
                    "0" to Entity(
                        mapOf(
                            "UserId" to Entity(
                                leaf = "diff"
                            )
                        )
                    )
                )
            )
        )
    )

    private val post1_diff = Entity(
        mapOf(
            "Post" to Entity(
                mapOf(
                    "1" to null
                )
            )
        )
    )

    private val post1_userid_diff = Entity(
        mapOf(
            "Post" to Entity(
                mapOf(
                    "1" to Entity(
                        mapOf(
                            "UserId" to Entity(
                                leaf = "diff"
                            )
                        )
                    )
                )
            )
        )
    )

    private val post1_new_key_diff = Entity(
        mapOf(
            "Post" to Entity(
                mapOf(
                    "1" to Entity(
                        mapOf(
                            "Comment" to Entity(
                                leaf = "new"
                            )
                        )
                    )
                )
            )
        )
    )

    @Test
    fun `test filter for creator1`() {
        assertEquals(a_filter_creator1, a.filter(creator1))
    }

    @Test
    fun `test filter for creator2`() {
        assertEquals(a_filter_creator2, a.filter(creator2))
    }

    @Test
    fun `test user0 is editable for creator1`() {
        assertEquals(true, a.validate(user0_diff, creator1))
    }

    @Test
    fun `test user0 is not editable for creator2`() {
        assertEquals(false, a.validate(user0_diff, creator2))
    }


    @Test
    fun `test user1 is editable for creator1`() {
        assertEquals(true, a.validate(user1_diff, creator1))
    }

    @Test
    fun `test user1 is editable for creator2`() {
        assertEquals(true, a.validate(user1_diff, creator2))
    }

    @Test
    fun `test post0 is editable for creator1 `() {
        assertEquals(true, a.validate(post0_diff, creator1))
    }

    @Test
    fun `test post0 is editable for creator2 `() {
        assertEquals(true, a.validate(post0_diff, creator2))
    }

    @Test
    fun `test post0 - UserId is  editable for creator1`() {
        assertEquals(true, a.validate(post0_userid_diff, creator1))
    }

    @Test
    fun `test post0 - UserId is not editable for creator2`() {
        assertEquals(false, a.validate(post0_userid_diff, creator2))
    }

    @Test
    fun `test post1 - UserId is editable for creator1`() {
        assertEquals(true, a.validate(post1_userid_diff, creator1))
    }

    @Test
    fun `test post1 - UserId is not editable for creator2`() {
        assertEquals(false, a.validate(post1_userid_diff, creator2))
    }

    @Test
    fun `test post1 is editable for creator1`() {
        assertEquals(true, a.validate(post1_diff, creator1))
    }

    @Test
    fun `test post1 is not editable for creator2`() {
        assertEquals(false, a.validate(post1_diff, creator2))
    }

    @Test
    fun `test creator1 can add new keys to post1`() {
        assertEquals(true, a.validate(post1_new_key_diff, creator1))
    }

    @Test
    fun `test creator2 can not add new keys to post1`() {
        assertEquals(false, a.validate(post1_new_key_diff, creator2))
    }
}