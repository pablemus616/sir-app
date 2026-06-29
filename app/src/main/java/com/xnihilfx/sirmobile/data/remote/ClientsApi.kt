package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.ClientDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ClientsApi {
    @GET("clients")
    suspend fun list(@Query("limit") limit: Int = 200): ApiEnvelope<Paginated<ClientDto>>
}
