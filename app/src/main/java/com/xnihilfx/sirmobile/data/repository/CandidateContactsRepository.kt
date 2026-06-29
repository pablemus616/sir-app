package com.xnihilfx.sirmobile.data.repository

import com.xnihilfx.sirmobile.data.remote.CandidateContactsApi
import com.xnihilfx.sirmobile.data.remote.dto.CandidateContactDto
import com.xnihilfx.sirmobile.data.remote.dto.CreateCandidateContactRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CandidateContactsRepository @Inject constructor(private val api: CandidateContactsApi) {

    /**
     * Devuelve el historial de contactos para un candidato dado.
     */
    open suspend fun historyFor(candidateId: Int): List<CandidateContactDto> =
        api.list(candidateId = candidateId).data?.items.orEmpty()

    /**
     * Registra un nuevo contacto con un candidato.
     */
    open suspend fun create(req: CreateCandidateContactRequest): CandidateContactDto =
        api.create(req).data ?: error("Respuesta inválida")
}
