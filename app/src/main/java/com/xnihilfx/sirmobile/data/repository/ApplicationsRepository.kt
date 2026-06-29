package com.xnihilfx.sirmobile.data.repository

import com.xnihilfx.sirmobile.data.remote.ApplicationsApi
import com.xnihilfx.sirmobile.data.remote.dto.ApplicationDto
import com.xnihilfx.sirmobile.data.remote.dto.ChangeStageRequest
import com.xnihilfx.sirmobile.data.remote.dto.CreateApplicationRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ApplicationsRepository @Inject constructor(private val api: ApplicationsApi) {

    /** Devuelve la primera aplicación que coincide con (candidateId, opportunityId), o null. */
    open suspend fun findFor(candidateId: Int, opportunityId: Int): ApplicationDto? =
        api.list(candidateId = candidateId, opportunityId = opportunityId, limit = 1)
            .data?.items?.firstOrNull()

    open suspend fun create(candidateId: Int, opportunityId: Int): ApplicationDto =
        api.create(CreateApplicationRequest(candidateId = candidateId, opportunityId = opportunityId))
            .data ?: error("Respuesta inválida al crear aplicación")

    open suspend fun changeStage(id: Int, stage: String): ApplicationDto =
        api.changeStage(id, ChangeStageRequest(stage))
            .data ?: error("Respuesta inválida al cambiar etapa")
}
