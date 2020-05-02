package ru.beryukhov.remote_domain.domain

import junit.framework.Assert.assertEquals
import org.junit.Test

class UserTest {
    @Test
    fun testRegexId(){
        val user = User(id = "-1", userName = "kek")
        assertEquals(user.id, getIdFromToString(user.toString()))
    }
}