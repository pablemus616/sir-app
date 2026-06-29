package com.xnihilfx.sirmobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xnihilfx.sirmobile.ui.candidatedetail.CandidateDetailScreen
import com.xnihilfx.sirmobile.ui.candidates.CandidatesScreen
import com.xnihilfx.sirmobile.ui.candidates.NewCandidateScreen
import com.xnihilfx.sirmobile.ui.login.LoginScreen
import com.xnihilfx.sirmobile.ui.opportunities.OpportunitiesScreen

@Composable
fun SirNavGraph(startRoute: String) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = startRoute) {
        composable(Route.Login.path) {
            LoginScreen(
                onAuthenticated = {
                    nav.navigate(Route.Opportunities.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Opportunities.path) {
            OpportunitiesScreen(
                onOpportunityClick = { oppId ->
                    nav.navigate(Route.Candidates.build(oppId))
                },
                onLogout = {
                    nav.navigate(Route.Login.path) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Route.Candidates.path,
            arguments = listOf(
                navArgument(Route.Candidates.ARG_OPP) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val oppId = backStackEntry.arguments?.getInt(Route.Candidates.ARG_OPP) ?: -1
            CandidatesScreen(
                opportunityId = oppId,
                onCandidateClick = { candId ->
                    nav.navigate(Route.CandidateDetail.build(candId, oppId))
                },
                onNewCandidate = {
                    nav.navigate(Route.NewCandidate.path)
                },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Route.NewCandidate.path) {
            NewCandidateScreen(
                onCreated = { _ ->
                    // Regresa a la lista de candidatos tras crear
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            route = Route.CandidateDetail.path,
            arguments = listOf(
                navArgument(Route.CandidateDetail.ARG_CAND) { type = NavType.IntType },
                navArgument(Route.CandidateDetail.ARG_OPP) { type = NavType.IntType; defaultValue = -1 },
            ),
        ) { backStackEntry ->
            val candId = backStackEntry.arguments?.getInt(Route.CandidateDetail.ARG_CAND) ?: -1
            val oppId = backStackEntry.arguments?.getInt(Route.CandidateDetail.ARG_OPP) ?: -1
            CandidateDetailScreen(
                candidateId = candId,
                opportunityId = oppId,
                onLogContact = { c, o -> nav.navigate(Route.LogContact.build(c, o)) },
                onMoveStage = { c, o -> nav.navigate(Route.MoveStage.build(c, o)) },
                onBack = { nav.popBackStack() },
            )
        }
        // LogContact, MoveStage — agregados en sus tareas
    }
}
