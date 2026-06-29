package com.xnihilfx.sirmobile
import com.xnihilfx.sirmobile.data.remote.ApiEnvelope
import com.xnihilfx.sirmobile.data.remote.Paginated
import com.xnihilfx.sirmobile.data.remote.parseErrorMessage
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
class EnvelopeAndErrorTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    @Test fun unwraps_paginated_envelope() {
        val body = """{"ok":true,"message":"Success","data":{"items":[{"id":1,"name":"call"}],"total":1,"page":1,"limit":20}}"""
        val env = json.decodeFromString<ApiEnvelope<Paginated<ContactTypeProbe>>>(body)
        assertEquals(true, env.ok); assertEquals(1, env.data?.total); assertEquals("call", env.data?.items?.first()?.name)
    }
    @Test fun extracts_error_message_from_envelope() {
        val body = """{"ok":false,"message":"Transición de etapa no permitida","path":"/api/applications/1/stage"}"""
        assertEquals("Transición de etapa no permitida", parseErrorMessage(json, body))
    }
    @kotlinx.serialization.Serializable data class ContactTypeProbe(val id: Int, val name: String)
}
