package com.xnihilfx.sirmobile

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.xnihilfx.sirmobile.data.local.SessionStore
import com.xnihilfx.sirmobile.data.remote.AuthApi
import com.xnihilfx.sirmobile.data.remote.AuthInterceptor
import com.xnihilfx.sirmobile.data.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit

class AuthRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = false }

    private fun retrofitFor(baseUrl: String, session: SessionStore): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor(session))
                    .build()
            )
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Test fun login_persists_tokens_and_employee() = runTest {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""{"ok":true,"message":"Success","data":{"accessToken":"A","refreshToken":"R"}}"""))
        server.enqueue(MockResponse().setBody("""{"ok":true,"message":"Success","data":{"id":3,"username":"ana","employeeId":12,"roles":[{"id":2,"name":"recruiter"}],"employee":{"id":12,"firstName":"Ana","lastName":"López"}}}"""))
        server.start()

        val session = SessionStore.inMemory()
        val api = retrofitFor(server.url("/").toString(), session).create(AuthApi::class.java)
        val repo = AuthRepository(api, session)

        repo.login("ana", "pw")

        assertEquals("A", session.accessToken)
        assertEquals(12, session.employeeId)
        assertEquals("Ana López", session.displayName)
        server.shutdown()
    }

    @Test fun logout_clears_session() = runTest {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""{"ok":true,"message":"Success","data":{"accessToken":"A","refreshToken":"R"}}"""))
        server.enqueue(MockResponse().setBody("""{"ok":true,"message":"Success","data":{"id":3,"username":"ana","employeeId":12,"roles":[],"employee":{"id":12,"firstName":"Ana","lastName":"López"}}}"""))
        server.enqueue(MockResponse().setBody("""{"ok":true,"message":"Success","data":null}"""))
        server.start()

        val session = SessionStore.inMemory()
        val api = retrofitFor(server.url("/").toString(), session).create(AuthApi::class.java)
        val repo = AuthRepository(api, session)

        repo.login("ana", "pw")
        assertNotNull(session.accessToken)

        repo.logout()
        assertNull(session.accessToken)
        assertNull(session.employeeId)
        server.shutdown()
    }
}
