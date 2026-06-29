package com.xnihilfx.sirmobile.data.repository

import com.xnihilfx.sirmobile.data.local.SessionStore
import com.xnihilfx.sirmobile.data.remote.AuthApi
import com.xnihilfx.sirmobile.data.remote.dto.LoginRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(private val api: AuthApi, private val session: SessionStore) {
    val loggedIn: Flow<Boolean> = session.sessionFlow
    fun employeeId(): Int? = session.employeeId
    fun displayName(): String? = session.displayName

    suspend fun login(username: String, password: String) {
        val tokens = api.login(LoginRequest(username.trim(), password)).data ?: error("Respuesta inválida")
        // Persist tokens first so the /me request is authenticated by the interceptor.
        session.save(tokens.accessToken, tokens.refreshToken, employeeId = -1, name = "")
        val me = api.me().data ?: error("Respuesta inválida")
        val emp = me.employee
        val name = listOfNotNull(emp?.firstName, emp?.lastName).joinToString(" ").ifBlank { me.username }
        session.save(tokens.accessToken, tokens.refreshToken, employeeId = me.employeeId, name = name)
    }

    suspend fun logout() { runCatching { api.logout() }; session.clear() }
}
