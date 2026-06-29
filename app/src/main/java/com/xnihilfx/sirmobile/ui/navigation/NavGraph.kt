package com.xnihilfx.sirmobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
        // Candidates, CandidateDetail, LogContact, NewCandidate, MoveStage — added en sus tasks
    }
}
