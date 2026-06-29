package com.xnihilfx.sirmobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContactTypeDto(val id: Int, val name: String)

@Serializable
data class CandidateContactDto(
    val id: Int,
    val candidateId: Int,
    val opportunityId: Int,
    val contactType: ContactTypeDto? = null,
    val contactTime: String,
    val callLength: Int? = null,
    val contactDesc: String? = null,
    val phoneNumberDialed: String? = null,
    val direction: String? = null,
    val recruiterEmployeeId: Int,
    val recruiter: EmployeeDto? = null,
    val createdAt: String? = null,
)

@Serializable
data class CreateCandidateContactRequest(
    val candidateId: Int,
    val opportunityId: Int,
    val contactType: Int,
    val contactTime: String,
    val direction: String? = null,
    val callLength: Int? = null,
    val contactDesc: String? = null,
    val phoneNumberDialed: String? = null,
)
