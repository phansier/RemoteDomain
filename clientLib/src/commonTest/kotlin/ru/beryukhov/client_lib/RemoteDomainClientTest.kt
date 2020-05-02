package ru.beryukhov.client_lib

import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteDomainClientTest {

    @Test
    fun testRegexDiff() {
        val defaultClientId = "-1"
        val newClientId = "42"
        assertEquals(
            getSampleJson(newClientId),
            replaceDefaultClientId(defaultClientId, newClientId, getSampleJson(defaultClientId))
        )
    }

    private fun getSampleJson(clientId: String) =
        "{\"data\":{\"User\":{\"data\":{\"$clientId" +
                "_0\":{\"leaf\":\"usr\"}}},\"Post\":{\"data\":{\"$clientId" +
                "_1\":{\"data\":{\"UserId\":{\"leaf\":\"$clientId" +
                "_0\"},\"Message\":{\"leaf\":\"m1\"}}}}}}}"
}