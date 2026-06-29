package com.xnihilfx.sirmobile.util

import android.content.Context
import android.provider.CallLog
import android.util.Log

/**
 * Lee el log de llamadas para recuperar la duración de la llamada recién hecha.
 * Requiere el permiso READ_CALL_LOG en tiempo de ejecución.
 *
 * No filtra por número de teléfono (el formato del número marcado vs. el guardado
 * en el log es poco fiable): la llamada recién realizada es la fila más reciente
 * posterior a [sinceMs] (la hora en que se pulsó "Llamar"). El sistema escribe la
 * fila con DURATION=0 y la actualiza ~1-2s tras colgar, así que el llamador debe
 * encuestar con reintentos hasta obtener un valor > 0.
 */
object CallLogReader {
    const val TAG = "SirCallLog"

    /** Duración (s) de la llamada más reciente con DATE >= [sinceMs], o null si aún no hay fila. */
    fun lastCallDuration(context: Context, sinceMs: Long): Int? {
        return try {
            val proj = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE,
            )
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                proj,
                "${CallLog.Calls.DATE} >= ?",
                arrayOf(sinceMs.toString()),
                "${CallLog.Calls.DATE} DESC",
            )?.use { c ->
                if (c.moveToFirst()) {
                    val number = c.getString(0)
                    val duration = c.getInt(1)
                    val date = c.getLong(2)
                    val type = c.getInt(3)
                    Log.d(TAG, "rows=${c.count} latest: number=$number duration=$duration date=$date type=$type (since=$sinceMs)")
                    duration
                } else {
                    Log.d(TAG, "sin filas de llamada desde $sinceMs")
                    null
                }
            } ?: run {
                Log.w(TAG, "cursor null al consultar el call log")
                null
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "READ_CALL_LOG denegado", e)
            null
        }
    }
}
