package com.xnihilfx.sirmobile.ui.navigation

sealed class Route(val path: String) {
    data object Login : Route("login")
    data object Opportunities : Route("opportunities")
    data object Candidates : Route("candidates?opportunityId={opportunityId}") {
        const val ARG_OPP = "opportunityId"
        fun build(opportunityId: Int) = "candidates?opportunityId=$opportunityId"
    }
    data object NewCandidate : Route("newCandidate")
    data object CandidateDetail : Route("candidate/{candidateId}?opportunityId={opportunityId}") {
        const val ARG_CAND = "candidateId"; const val ARG_OPP = "opportunityId"
        fun build(candidateId: Int, opportunityId: Int) = "candidate/$candidateId?opportunityId=$opportunityId"
    }
    data object LogContact : Route("logContact/{candidateId}/{opportunityId}?phone={phone}&email={email}") {
        const val ARG_CAND = "candidateId"
        const val ARG_OPP = "opportunityId"
        const val ARG_PHONE = "phone"
        const val ARG_EMAIL = "email"
        fun build(
            candidateId: Int,
            opportunityId: Int,
            phone: String? = null,
            email: String? = null,
        ): String {
            val base = "logContact/$candidateId/$opportunityId"
            val params = listOfNotNull(
                phone?.takeIf { it.isNotBlank() }
                    ?.let { "phone=${java.net.URLEncoder.encode(it, "UTF-8")}" },
                email?.takeIf { it.isNotBlank() }
                    ?.let { "email=${java.net.URLEncoder.encode(it, "UTF-8")}" },
            )
            return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
        }
    }
    data object MoveStage : Route("moveStage/{candidateId}/{opportunityId}") {
        const val ARG_CAND = "candidateId"; const val ARG_OPP = "opportunityId"
        fun build(candidateId: Int, opportunityId: Int) = "moveStage/$candidateId/$opportunityId"
    }
}
