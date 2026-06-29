package com.xnihilfx.sirmobile.util

import android.content.Context
import android.provider.CallLog

/**
 * Lee el log de llamadas para recuperar la duración de la llamada más reciente.
 * Requiere el permiso READ_CALL_LOG en tiempo de ejecución.
 */
object CallLogReader {
    /**
     * Devuelve la duración (en segundos) de la llamada más reciente al [number]
     * dentro de los últimos [withinMs] milisegundos, o null si no se encuentra
     * o el permiso fue denegado.
     */
    fun lastCallDuration(context: Context, number: String?, withinMs: Long = 60 * 60 * 1000): Int? {
        return try {
            val since = System.currentTimeMillis() - withinMs
            val proj = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE)
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                proj,
                "${CallLog.Calls.DATE} >= ?",
                arrayOf(since.toString()),
                "${CallLog.Calls.DATE} DESC",
            )?.use { c ->
                val digits = number?.filter { it.isDigit() }?.takeLast(8)
                while (c.moveToNext()) {
                    val num = c.getString(0)?.filter { it.isDigit() }?.takeLast(8)
                    if (digits == null || num == digits) return c.getInt(1)
                }
                null
            }
        } catch (_: SecurityException) {
            null
        }
    }
}
