package com.xnihilfx.sirmobile.data.repository

import com.xnihilfx.sirmobile.data.remote.ContactTypesApi
import com.xnihilfx.sirmobile.data.remote.dto.ContactTypeDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ContactTypesRepository @Inject constructor(private val api: ContactTypesApi) {

    /** Devuelve todos los tipos de contacto disponibles (call, email, meeting, whatsapp). */
    open suspend fun all(): List<ContactTypeDto> = api.list().data?.items.orEmpty()
}
