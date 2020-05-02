package ru.beryukhov.remote_domain.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.beryukhov.client_lib.replaceDefaultClientId

class UserTest {
    @Test
    fun testRegexId(){
        val user = User(id = "-1", userName = "kek")
        assertEquals(user.id, getIdFromToString(user.toString()))
    }
}