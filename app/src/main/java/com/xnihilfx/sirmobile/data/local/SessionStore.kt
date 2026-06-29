package com.xnihilfx.sirmobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "sir_session")

@Singleton
class SessionStore private constructor(private val persist: Persistence) {

    interface Persistence {
        suspend fun read(): Map<String, String>
        suspend fun write(values: Map<String, String>)
        suspend fun clearAll()
    }

    @Volatile var accessToken: String? = null; private set
    @Volatile var refreshToken: String? = null; private set
    @Volatile var employeeId: Int? = null; private set
    @Volatile var displayName: String? = null; private set

    private val _loggedIn = MutableStateFlow(false)
    val sessionFlow: Flow<Boolean> = _loggedIn.asStateFlow()

    suspend fun load() {
        val m = persist.read()
        accessToken = m[K_ACCESS]; refreshToken = m[K_REFRESH]
        employeeId = m[K_EMP]?.toIntOrNull(); displayName = m[K_NAME]
        _loggedIn.value = accessToken != null
    }

    suspend fun save(access: String, refresh: String, employeeId: Int, name: String) {
        accessToken = access; refreshToken = refresh; this.employeeId = employeeId; displayName = name
        persist.write(mapOf(K_ACCESS to access, K_REFRESH to refresh, K_EMP to employeeId.toString(), K_NAME to name))
        _loggedIn.value = true
    }

    suspend fun updateTokens(access: String, refresh: String) {
        accessToken = access; refreshToken = refresh
        persist.write(mapOf(K_ACCESS to access, K_REFRESH to refresh))
    }

    suspend fun clear() {
        accessToken = null; refreshToken = null; employeeId = null; displayName = null
        persist.clearAll(); _loggedIn.value = false
    }

    companion object {
        private const val K_ACCESS = "access"; private const val K_REFRESH = "refresh"
        private const val K_EMP = "emp"; private const val K_NAME = "name"

        fun inMemory(): SessionStore = SessionStore(object : Persistence {
            private val m = mutableMapOf<String, String>()
            override suspend fun read() = m.toMap()
            override suspend fun write(values: Map<String, String>) { m.putAll(values) }
            override suspend fun clearAll() { m.clear() }
        })

        fun create(context: Context): SessionStore {
            val ds = context.dataStore
            return SessionStore(object : Persistence {
                override suspend fun read(): Map<String, String> {
                    val p = ds.data.first()
                    return buildMap {
                        p[stringPreferencesKey(K_ACCESS)]?.let { put(K_ACCESS, it) }
                        p[stringPreferencesKey(K_REFRESH)]?.let { put(K_REFRESH, it) }
                        p[stringPreferencesKey(K_EMP)]?.let { put(K_EMP, it) }
                        p[stringPreferencesKey(K_NAME)]?.let { put(K_NAME, it) }
                    }
                }
                override suspend fun write(values: Map<String, String>) {
                    ds.edit { e -> values.forEach { (k, v) -> e[stringPreferencesKey(k)] = v } }
                }
                override suspend fun clearAll() { ds.edit { it.clear() } }
            })
        }
    }
}
