package com.xnihilfx.sirmobile.data.repository

import com.xnihilfx.sirmobile.data.remote.CandidatesApi
import com.xnihilfx.sirmobile.data.remote.dto.CandidateDto
import com.xnihilfx.sirmobile.data.remote.dto.CreateCandidateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CandidatesRepository @Inject constructor(private val api: CandidatesApi) {

    suspend fun search(name: String? = null, status: String? = null): List<CandidateDto> =
        api.search(
            name = name?.takeIf { it.isNotBlank() },
            status = status,
        ).data?.items.orEmpty()

    suspend fun get(id: Int): CandidateDto =
        api.get(id).data ?: error("Candidato no encontrado")

    suspend fun create(req: CreateCandidateRequest): CandidateDto =
        api.create(req).data ?: error("Respuesta inválida")
}
