package com.xnihilfx.sirmobile.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xnihilfx.sirmobile.ui.login.LoginScreen

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
            // OpportunitiesScreen — Task 7
            Text(text = "Puestos (próximamente)")
        }
        // Candidates, CandidateDetail, LogContact, NewCandidate, MoveStage — added in their tasks
    }
}
