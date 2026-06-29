package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.local.SessionStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val session: SessionStore,
    private val json: Json,
    private val baseUrl: String,
) : Authenticator {
    private val refreshClient = OkHttpClient()
    @Synchronized override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null            // already retried once
        val current = session.accessToken
        // If another thread already refreshed, just retry with the new token.
        val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        if (current != null && current != sentToken) {
            return response.request.newBuilder().header("Authorization", "Bearer $current").build()
        }
        val refresh = session.refreshToken ?: return null
        val newTokens = runCatching { refreshBlocking(refresh) }.getOrNull()
        if (newTokens == null) { runBlocking { session.clear() }; return null }
        runBlocking { session.updateTokens(newTokens.accessToken, newTokens.refreshToken) }
        return response.request.newBuilder().header("Authorization", "Bearer ${newTokens.accessToken}").build()
    }

    private fun refreshBlocking(refreshToken: String): com.xnihilfx.sirmobile.data.remote.dto.AuthTokens {
        val body = """{"refreshToken":"$refreshToken"}""".toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(baseUrl.trimEnd('/') + "/auth/refresh").post(body).build()
        refreshClient.newCall(req).execute().use { r ->
            val text = r.body?.string().orEmpty()
            require(r.isSuccessful) { "refresh failed ${r.code}" }
            val env = json.decodeFromString<ApiEnvelope<com.xnihilfx.sirmobile.data.remote.dto.AuthTokens>>(text)
            return env.data ?: error("no tokens")
        }
    }

    private fun responseCount(response: Response): Int {
        var r: Response? = response; var c = 1
        while (r?.priorResponse != null) { c++; r = r.priorResponse }
        return c
    }
}
