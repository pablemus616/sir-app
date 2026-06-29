package com.xnihilfx.sirmobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CandidateDto(
    val id: Int,
    val firstName: String,
    val secondName: String? = null,
    val lastName: String,
    val surName: String? = null,
    val nationalId: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val headline: String? = null,
    val source: String? = null,
    val status: String = "new",
    val notes: String? = null,
    val createdAt: String? = null,
) {
    val fullName: String
        get() = listOfNotNull(firstName, secondName, lastName, surName).joinToString(" ")
}

@Serializable
data class CreateCandidateRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val source: String? = null,
)
