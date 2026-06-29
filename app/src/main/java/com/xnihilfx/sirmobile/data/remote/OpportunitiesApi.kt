package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.CreateOpportunityRequest
import com.xnihilfx.sirmobile.data.remote.dto.OpportunityDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OpportunitiesApi {
    @GET("opportunities")
    suspend fun list(
        @Query("status") status: String? = "open",
        @Query("responsibleEmployeeId") responsibleEmployeeId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ApiEnvelope<Paginated<OpportunityDto>>

    @POST("opportunities")
    suspend fun create(@Body body: CreateOpportunityRequest): ApiEnvelope<OpportunityDto>
}
