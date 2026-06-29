package com.xnihilfx.sirmobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable data class LoginRequest(val username: String, val password: String)
@Serializable data class RefreshRequest(val refreshToken: String)
@Serializable data class AuthTokens(val accessToken: String, val refreshToken: String)
@Serializable data class RoleDto(val id: Int, val name: String)
@Serializable data class EmployeeDto(val id: Int, val firstName: String, val secondName: String? = null, val lastName: String, val surName: String? = null, val phoneNumber: String? = null, val email: String? = null)
@Serializable data class MeDto(val id: Int, val username: String, val employeeId: Int, val roles: List<RoleDto> = emptyList(), val employee: EmployeeDto? = null)
