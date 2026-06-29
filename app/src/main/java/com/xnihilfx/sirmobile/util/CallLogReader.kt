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
     * registrada a partir de [sinceMs] (epoch ms, normalmente la hora en que se
     * marcó), o null si todavía no hay una entrada o el permiso fue denegado.
     *
     * Nota: el sistema telefónico escribe la fila del call log con DURATION=0 al
     * iniciar y la actualiza ~1-2s después de colgar; por eso el llamador debe
     * encuestar este método con reintentos hasta obtener un valor > 0.
     */
    fun lastCallDuration(context: Context, number: String?, sinceMs: Long): Int? {
        return try {
            val proj = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE)
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                proj,
                "${CallLog.Calls.DATE} >= ?",
                arrayOf(sinceMs.toString()),
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
