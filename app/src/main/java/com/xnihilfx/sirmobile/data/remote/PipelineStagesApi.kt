package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.PipelineStageDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PipelineStagesApi {
    @GET("pipeline-stages")
    suspend fun list(@Query("limit") limit: Int = 200): ApiEnvelope<Paginated<PipelineStageDto>>
}
