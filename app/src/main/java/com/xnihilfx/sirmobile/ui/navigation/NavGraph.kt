package com.xnihilfx.sirmobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        // CandidateDetail, LogContact, MoveStage — agregados en sus tareas
    }
}
