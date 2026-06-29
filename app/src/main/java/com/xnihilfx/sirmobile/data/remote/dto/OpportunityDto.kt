package com.xnihilfx.sirmobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    val id: Int,
    val name: String,
    val sector: String? = null,
)

@Serializable
data class AreaDto(
    val id: Int,
    val name: String,
)

@Serializable
data class PipelineStageDto(
    val id: Int,
    val name: String,
    val sortOrder: Int = 0,
    val isWon: Boolean = false,
    val isLost: Boolean = false,
)

@Serializable
data class CreateOpportunityRequest(
    val clientId: Int,
    val responsibleEmployeeId: Int,
    val pipelineStageId: Int,
    val title: String? = null,
    val areaId: Int? = null,
    val headcount: Int? = null,
    val seniority: String? = null,
)

@Serializable
data class OpportunityDto(
    val id: Int,
    val title: String? = null,
    val status: String,
    val headcount: Int = 1,
    val seniority: String? = null,
    val amount: Double? = null,
    val currency: String = "GTQ",
    val responsibleEmployeeId: Int,
    val clientId: Int,
    val client: ClientDto? = null,
    val areaId: Int? = null,
    val area: AreaDto? = null,
    val pipelineStageId: Int,
    val pipelineStage: PipelineStageDto? = null,
    val responsibleEmployee: EmployeeDto? = null,
    val lastContactAt: String? = null,
    val nextFollowUpAt: String? = null,
    val createdAt: String? = null,
)
