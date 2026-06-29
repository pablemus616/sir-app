package com.xnihilfx.sirmobile.data.repository

import com.xnihilfx.sirmobile.data.remote.CandidateContactsApi
import com.xnihilfx.sirmobile.data.remote.dto.CandidateContactDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CandidateContactsRepository @Inject constructor(private val api: CandidateContactsApi) {

    /**
     * Devuelve el historial de contactos para un candidato dado.
     * El método `create` se añade en la Tarea 10.
     */
    suspend fun historyFor(candidateId: Int): List<CandidateContactDto> =
        api.list(candidateId = candidateId).data?.items.orEmpty()
}
