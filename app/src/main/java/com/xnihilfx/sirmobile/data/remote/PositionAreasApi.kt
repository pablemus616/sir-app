package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.AreaDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PositionAreasApi {
    @GET("position-areas")
    suspend fun list(@Query("limit") limit: Int = 200): ApiEnvelope<Paginated<AreaDto>>
}
