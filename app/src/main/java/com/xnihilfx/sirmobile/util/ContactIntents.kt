package com.xnihilfx.sirmobile.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Lanza intents del sistema para llamar, WhatsApp o enviar email a un candidato.
 * Usar ACTION_DIAL (no ACTION_CALL) para no requerir permiso CALL_PHONE.
 */
object ContactIntents {

    /** Abre el marcador con el número dado. No requiere permiso CALL_PHONE. */
    fun dial(context: Context, phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    }

    /**
     * Abre WhatsApp para chatear con el número dado.
     * Normaliza a número guatemalteco (8 dígitos → 502xxxxxxxx) si aplica.
     */
    fun whatsapp(context: Context, phone: String) {
        val normalized = normalizeGt(phone)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$normalized"))
        context.startActivity(intent)
    }

    /** Abre el cliente de correo con la dirección dada. */
    fun email(context: Context, address: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
        context.startActivity(intent)
    }

    /**
     * Elimina caracteres no numéricos y añade el prefijo 502 si el número
     * tiene exactamente 8 dígitos (formato guatemalteco local).
     */
    private fun normalizeGt(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.length == 8) "502$digits" else digits
    }
}
