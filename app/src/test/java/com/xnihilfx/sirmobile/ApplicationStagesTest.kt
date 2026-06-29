package com.xnihilfx.sirmobile

import com.xnihilfx.sirmobile.domain.ApplicationStages
import org.junit.Assert.*
import org.junit.Test

class ApplicationStagesTest {

    @Test
    fun applied_transitions_to_screening_rejected_withdrawn() {
        assertEquals(
            listOf("screening", "rejected", "withdrawn"),
            ApplicationStages.nextLegal("applied"),
        )
    }

    @Test
    fun screening_transitions_to_interview_rejected_withdrawn() {
        assertEquals(
            listOf("interview", "rejected", "withdrawn"),
            ApplicationStages.nextLegal("screening"),
        )
    }

    @Test
    fun interview_transitions_to_offer_rejected_withdrawn() {
        assertEquals(
            listOf("offer", "rejected", "withdrawn"),
            ApplicationStages.nextLegal("interview"),
        )
    }

    @Test
    fun offer_transitions_to_hired_rejected_withdrawn() {
        assertEquals(
            listOf("hired", "rejected", "withdrawn"),
            ApplicationStages.nextLegal("offer"),
        )
    }

    @Test
    fun terminal_stages_have_no_transitions() {
        assertTrue(ApplicationStages.nextLegal("hired").isEmpty())
        assertTrue(ApplicationStages.nextLegal("rejected").isEmpty())
        assertTrue(ApplicationStages.nextLegal("withdrawn").isEmpty())
    }

    @Test
    fun unknown_stage_returns_empty() {
        assertTrue(ApplicationStages.nextLegal("unknown").isEmpty())
    }

    @Test
    fun labels_are_in_spanish() {
        assertEquals("Postulado",  ApplicationStages.label("applied"))
        assertEquals("Filtro",     ApplicationStages.label("screening"))
        assertEquals("Entrevista", ApplicationStages.label("interview"))
        assertEquals("Oferta",     ApplicationStages.label("offer"))
        assertEquals("Contratado", ApplicationStages.label("hired"))
        assertEquals("Rechazado",  ApplicationStages.label("rejected"))
        assertEquals("Retirado",   ApplicationStages.label("withdrawn"))
    }
}
