package com.xnihilfx.sirmobile.data.remote
import kotlinx.serialization.Serializable
@Serializable data class ApiEnvelope<T>(val ok: Boolean = false, val message: String? = null, val data: T? = null)
@Serializable data class Paginated<T>(val items: List<T> = emptyList(), val total: Int = 0, val page: Int = 1, val limit: Int = 20)
