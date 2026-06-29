package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.local.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val session: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val token = session.accessToken
        val path = req.url.encodedPath
        if (token == null || req.header("Authorization") != null || path.endsWith("auth/login") || path.endsWith("auth/refresh")) {
            return chain.proceed(req)
        }
        return chain.proceed(req.newBuilder().header("Authorization", "Bearer $token").build())
    }
}
