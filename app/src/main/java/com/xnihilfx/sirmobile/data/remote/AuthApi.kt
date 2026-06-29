package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.*
import retrofit2.http.*

interface AuthApi {
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): ApiEnvelope<AuthTokens>
    @POST("auth/refresh") suspend fun refresh(@Body body: RefreshRequest): ApiEnvelope<AuthTokens>
    @GET("auth/me") suspend fun me(): ApiEnvelope<MeDto>
    @POST("auth/logout") suspend fun logout(): ApiEnvelope<Unit>
}
