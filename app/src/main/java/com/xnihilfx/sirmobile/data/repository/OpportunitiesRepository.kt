package com.xnihilfx.sirmobile.data.repository

import com.xnihilfx.sirmobile.data.remote.OpportunitiesApi
import com.xnihilfx.sirmobile.data.remote.dto.OpportunityDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpportunitiesRepository @Inject constructor(private val api: OpportunitiesApi) {
    suspend fun openOpportunities(mineOnly: Boolean, employeeId: Int?): List<OpportunityDto> =
        api.list(
            status = "open",
            responsibleEmployeeId = if (mineOnly) employeeId else null,
            page = 1,
            limit = 100,
        ).data?.items.orEmpty()
}
