package com.xnihilfx.sirmobile

import com.xnihilfx.sirmobile.data.local.SessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SessionStoreTest {
    @Test fun save_then_cache_exposes_tokens_and_employee() = runTest {
        val store = SessionStore.inMemory()           // test factory backing to a MutableMap
        store.save(access = "a1", refresh = "r1", employeeId = 7, name = "Ana")
        assertEquals("a1", store.accessToken); assertEquals("r1", store.refreshToken)
        assertEquals(7, store.employeeId); assertEquals("Ana", store.displayName)
        store.updateTokens("a2", "r2")
        assertEquals("a2", store.accessToken); assertEquals("r2", store.refreshToken)
        store.clear()
        assertNull(store.accessToken); assertNull(store.employeeId)
    }
}
