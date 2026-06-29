package com.xnihilfx.sirmobile.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.xnihilfx.sirmobile.ui.candidatedetail.CandidateDetailScreen
import com.xnihilfx.sirmobile.ui.candidatedetail.CandidateDetailViewModel
import com.xnihilfx.sirmobile.ui.candidates.CandidatesScreen
import com.xnihilfx.sirmobile.ui.candidates.NewCandidateScreen
import com.xnihilfx.sirmobile.ui.login.LoginScreen
import com.xnihilfx.sirmobile.ui.logcontact.LogContactScreen
import com.xnihilfx.sirmobile.ui.movestage.MoveStageScreen
import com.xnihilfx.sirmobile.ui.newopportunity.NewOpportunityScreen
import com.xnihilfx.sirmobile.ui.opportunities.OpportunitiesScreen
import com.xnihilfx.sirmobile.ui.opportunities.OpportunitiesViewModel

@Composable
fun SirNavGraph(startRoute: String) {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = startRoute,
        enterTransition = {
            slideInHorizontally(tween(280)) { it / 6 } + fadeIn(tween(280))
        },
        exitTransition = {
            fadeOut(tween(180))
        },
        popEnterTransition = {
            fadeIn(tween(220))
        },
        popExitTransition = {
            slideOutHorizontally(tween(260)) { it / 6 } + fadeOut(tween(220))
        },
    ) {
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
                onNewOpportunity = {
                    nav.navigate(Route.NewOpportunity.path)
                },
            )
        }
        composable(Route.NewOpportunity.path) {
            val oppsVm: OpportunitiesViewModel = hiltViewModel(nav.getBackStackEntry(Route.Opportunities.path))
            NewOpportunityScreen(
                onCreated = {
                    oppsVm.refresh()
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() },
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
            // Elevamos el ViewModel para poder leer el teléfono/email del candidato
            // al navegar a LogContact (accesos rápidos de contacto).
            val detailVm: CandidateDetailViewModel = hiltViewModel()
            val detailState by detailVm.state.collectAsStateWithLifecycle()
            CandidateDetailScreen(
                candidateId = candId,
                opportunityId = oppId,
                onLogContact = { c, o ->
                    nav.navigate(
                        Route.LogContact.build(
                            candidateId = c,
                            opportunityId = o,
                            phone = detailState.candidate?.phoneNumber,
                            email = detailState.candidate?.email,
                        ),
                    )
                },
                onMoveStage = { c, o -> nav.navigate(Route.MoveStage.build(c, o)) },
                onBack = { nav.popBackStack() },
                viewModel = detailVm,
            )
        }
        composable(
            route = Route.LogContact.path,
            arguments = listOf(
                navArgument(Route.LogContact.ARG_CAND) { type = NavType.IntType },
                navArgument(Route.LogContact.ARG_OPP) { type = NavType.IntType },
                navArgument(Route.LogContact.ARG_PHONE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Route.LogContact.ARG_EMAIL) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            LogContactScreen(
                onSaved = { nav.popBackStack() },
                onBack = { nav.popBackStack() },
            )
        }
        // MoveStage — Tarea 11
        composable(
            route = Route.MoveStage.path,
            arguments = listOf(
                navArgument(Route.MoveStage.ARG_CAND) { type = NavType.IntType },
                navArgument(Route.MoveStage.ARG_OPP) { type = NavType.IntType },
            ),
        ) {
            MoveStageScreen(
                onDone = { nav.popBackStack() },
                onBack = { nav.popBackStack() },
            )
        }
    }
}
