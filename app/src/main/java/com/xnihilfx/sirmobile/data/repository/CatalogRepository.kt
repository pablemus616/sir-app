package com.xnihilfx.sirmobile.data.repository

import com.xnihilfx.sirmobile.data.remote.ClientsApi
import com.xnihilfx.sirmobile.data.remote.PipelineStagesApi
import com.xnihilfx.sirmobile.data.remote.PositionAreasApi
import com.xnihilfx.sirmobile.data.remote.dto.AreaDto
import com.xnihilfx.sirmobile.data.remote.dto.ClientDto
import com.xnihilfx.sirmobile.data.remote.dto.PipelineStageDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    private val clientsApi: ClientsApi,
    private val areasApi: PositionAreasApi,
    private val stagesApi: PipelineStagesApi,
) {
    suspend fun clients(): List<ClientDto> = clientsApi.list().data?.items.orEmpty()
    suspend fun areas(): List<AreaDto> = areasApi.list().data?.items.orEmpty()
    suspend fun stages(): List<PipelineStageDto> = stagesApi.list().data?.items.orEmpty()
}
