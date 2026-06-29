package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.ApplicationDto
import com.xnihilfx.sirmobile.data.remote.dto.ChangeStageRequest
import com.xnihilfx.sirmobile.data.remote.dto.CreateApplicationRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApplicationsApi {
    @GET("applications")
    suspend fun list(
        @Query("candidateId") candidateId: Int? = null,
        @Query("opportunityId") opportunityId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): ApiEnvelope<Paginated<ApplicationDto>>

    @POST("applications")
    suspend fun create(@Body body: CreateApplicationRequest): ApiEnvelope<ApplicationDto>

    @PATCH("applications/{id}/stage")
    suspend fun changeStage(
        @Path("id") id: Int,
        @Body body: ChangeStageRequest,
    ): ApiEnvelope<ApplicationDto>
}
