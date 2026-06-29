package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.CandidateDto
import com.xnihilfx.sirmobile.data.remote.dto.CreateCandidateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CandidatesApi {
    @GET("candidates")
    suspend fun search(
        @Query("name") name: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30,
    ): ApiEnvelope<Paginated<CandidateDto>>

    @GET("candidates/{id}")
    suspend fun get(@Path("id") id: Int): ApiEnvelope<CandidateDto>

    @POST("candidates")
    suspend fun create(@Body body: CreateCandidateRequest): ApiEnvelope<CandidateDto>
}
