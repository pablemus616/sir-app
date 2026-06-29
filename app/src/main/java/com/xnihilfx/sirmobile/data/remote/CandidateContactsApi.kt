package com.xnihilfx.sirmobile.data.remote

import com.xnihilfx.sirmobile.data.remote.dto.CandidateContactDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CandidateContactsApi {

    @GET("candidate-contacts")
    suspend fun list(
        @Query("candidateId") candidateId: Int? = null,
        @Query("opportunityId") opportunityId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): ApiEnvelope<Paginated<CandidateContactDto>>
}
