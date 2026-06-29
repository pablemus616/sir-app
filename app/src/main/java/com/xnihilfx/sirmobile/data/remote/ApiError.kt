package com.xnihilfx.sirmobile.data.remote
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
class ApiException(val userMessage: String, val code: Int? = null) : RuntimeException(userMessage)
@kotlinx.serialization.Serializable private data class ErrorBody(val ok: Boolean = false, val message: String? = null)
fun parseErrorMessage(json: Json, body: String?): String? =
    body?.takeIf { it.isNotBlank() }?.let { runCatching { json.decodeFromString<ErrorBody>(it).message }.getOrNull() }
fun Throwable.toUserMessage(json: Json): String = when (this) {
    is ApiException -> userMessage
    is HttpException -> {
        val raw = response()?.errorBody()?.string()
        parseErrorMessage(json, raw) ?: when (code()) {
            401 -> "Sesión expirada. Inicia sesión de nuevo."
            403 -> "No tienes permiso para esta acción."
            404 -> "No encontrado."
            409 -> "Registro duplicado."
            in 500..599 -> "Error del servidor. Intenta más tarde."
            else -> "Error ${code()}."
        }
    }
    is IOException -> "Sin conexión. Revisa tu internet."
    else -> message ?: "Error desconocido."
}
