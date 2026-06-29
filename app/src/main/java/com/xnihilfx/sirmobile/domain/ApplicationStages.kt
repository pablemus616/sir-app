package com.xnihilfx.sirmobile.domain

/** Espejo exacto de APPLICATION_TRANSITIONS del backend. */
object ApplicationStages {
    val transitions = mapOf(
        "applied"   to listOf("screening", "rejected", "withdrawn"),
        "screening" to listOf("interview", "rejected", "withdrawn"),
        "interview" to listOf("offer",     "rejected", "withdrawn"),
        "offer"     to listOf("hired",     "rejected", "withdrawn"),
        "hired"     to emptyList(),
        "rejected"  to emptyList(),
        "withdrawn" to emptyList(),
    )

    fun nextLegal(stage: String): List<String> = transitions[stage].orEmpty()

    fun label(stage: String): String = when (stage) {
        "applied"   -> "Postulado"
        "screening" -> "Filtro"
        "interview" -> "Entrevista"
        "offer"     -> "Oferta"
        "hired"     -> "Contratado"
        "rejected"  -> "Rechazado"
        "withdrawn" -> "Retirado"
        else        -> stage
    }
}
