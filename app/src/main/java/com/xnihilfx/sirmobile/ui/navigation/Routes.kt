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
    data object LogContact : Route("logContact/{candidateId}/{opportunityId}") {
        const val ARG_CAND = "candidateId"; const val ARG_OPP = "opportunityId"
        fun build(candidateId: Int, opportunityId: Int) = "logContact/$candidateId/$opportunityId"
    }
    data object MoveStage : Route("moveStage/{candidateId}/{opportunityId}") {
        const val ARG_CAND = "candidateId"; const val ARG_OPP = "opportunityId"
        fun build(candidateId: Int, opportunityId: Int) = "moveStage/$candidateId/$opportunityId"
    }
}
