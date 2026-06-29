package com.xnihilfx.sirmobile

import com.xnihilfx.sirmobile.data.local.SessionStore
import com.xnihilfx.sirmobile.data.remote.AuthInterceptor
import com.xnihilfx.sirmobile.data.remote.TokenAuthenticator
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class AuthFlowTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun client(session: SessionStore, baseUrl: String) = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(session))
        .authenticator(TokenAuthenticator(session, json, baseUrl))
        .build()

    @Test fun attaches_bearer_token() = runTest {
        val server = MockWebServer(); server.enqueue(MockResponse().setResponseCode(200).setBody("{}")); server.start()
        val session = SessionStore.inMemory().apply { save("ACC","REF",1,"x") }
        client(session, server.url("/").toString()).newCall(Request.Builder().url(server.url("/candidates")).build()).execute()
        val recorded = server.takeRequest()
        assertEquals("Bearer ACC", recorded.getHeader("Authorization"))
        server.shutdown()
    }

    @Test fun on_401_refreshes_then_retries_with_new_token() = runTest {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"ok":false,"message":"unauthorized"}"""))      // first call
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true,"message":"Success","data":{"accessToken":"NEW","refreshToken":"NEWREF"}}""")) // refresh
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))                                              // retry
        server.start()
        val session = SessionStore.inMemory().apply { save("OLD","REF",1,"x") }
        val resp = client(session, server.url("/").toString()).newCall(Request.Builder().url(server.url("/candidates")).build()).execute()
        assertEquals(200, resp.code)
        server.takeRequest(); val refreshReq = server.takeRequest(); val retry = server.takeRequest()
        assertTrue(refreshReq.path!!.endsWith("auth/refresh"))
        assertEquals("Bearer NEW", retry.getHeader("Authorization"))
        assertEquals("NEW", session.accessToken)
        server.shutdown()
    }

    @Test fun on_refresh_failure_clears_session_and_gives_up() = runTest {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))                                    // first call
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"ok":false,"message":"bad refresh"}""")) // refresh fails
        server.start()
        val session = SessionStore.inMemory().apply { save("OLD","REF",1,"x") }
        val resp = client(session, server.url("/").toString()).newCall(Request.Builder().url(server.url("/candidates")).build()).execute()
        assertEquals(401, resp.code)
        assertNull(session.accessToken)
        server.shutdown()
    }
}
