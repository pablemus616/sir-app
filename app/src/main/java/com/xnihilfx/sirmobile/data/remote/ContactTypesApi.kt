package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.ContactTypeDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ContactTypesApi {

    @GET("contact-types")
    suspend fun list(@Query("limit") limit: Int = 50): ApiEnvelope<Paginated<ContactTypeDto>>
}
