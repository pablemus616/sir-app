package com.xnihilfx.sirmobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDto(
    val id: Int,
    val candidateId: Int,
    val opportunityId: Int,
    val stage: String,
    val source: String? = null,
    val notes: String? = null,
    val appliedAt: String? = null,
)

@Serializable
data class CreateApplicationRequest(
    val candidateId: Int,
    val opportunityId: Int,
    val stage: String? = null,
)

@Serializable
data class ChangeStageRequest(val stage: String)
