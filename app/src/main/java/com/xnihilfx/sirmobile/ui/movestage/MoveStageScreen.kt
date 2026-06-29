package com.xnihilfx.sirmobile.ui.movestage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xnihilfx.sirmobile.domain.ApplicationStages
import com.xnihilfx.sirmobile.ui.components.EmptyView
import com.xnihilfx.sirmobile.ui.components.ErrorView
import com.xnihilfx.sirmobile.ui.components.LoadingView
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveStageScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: MoveStageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Eventos de un solo disparo
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MoveStageEvent.Moved -> onDone()
                is MoveStageEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mover etapa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Regresar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        val contentKey = when {
            state.loading -> 0
            state.error != null && state.application == null -> 1
            state.application == null -> 2
            else -> 3
        }
        AnimatedContent(
            targetState = contentKey,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "move_content",
        ) { key ->
            when (key) {
                0 -> LoadingView(modifier = Modifier.padding(paddingValues))

                1 -> ErrorView(
                    message = state.error.orEmpty(),
                    onRetry = viewModel::load,
                    modifier = Modifier.padding(paddingValues),
                )

                // No existe aplicación todavía → ofrecer crearla
                2 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        text = "Sin aplicación registrada",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Este candidato aún no tiene una aplicación para este puesto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = viewModel::createApplication,
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Crossfade(targetState = state.saving, label = "create_app") { saving ->
                            if (saving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text("Crear aplicación")
                            }
                        }
                    }
                }

                // Existe aplicación → mostrar etapa actual y posibles siguientes
                else -> {
                    val app = state.application
                    if (app != null) {
                        val currentLabel = ApplicationStages.label(app.stage)
                        val isTerminal = state.legalNext.isEmpty()

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Etapa actual",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentLabel,
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }

                            if (isTerminal) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EmptyView(text = "Esta etapa es terminal. No hay más movimientos posibles.")
                                }
                            } else {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Mover a:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                items(state.legalNext) { nextStage ->
                                    val isNegative = nextStage == "rejected" || nextStage == "withdrawn"
                                    if (isNegative) {
                                        OutlinedButton(
                                            onClick = { viewModel.move(nextStage) },
                                            enabled = !state.saving,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error,
                                            ),
                                        ) {
                                            Crossfade(targetState = state.saving, label = "move_neg") { saving ->
                                                if (saving) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp,
                                                    )
                                                } else {
                                                    Text(ApplicationStages.label(nextStage))
                                                }
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.move(nextStage) },
                                            enabled = !state.saving,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Crossfade(targetState = state.saving, label = "move_pos") { saving ->
                                                if (saving) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                    )
                                                } else {
                                                    Text(ApplicationStages.label(nextStage))
                                                }
                                            }
                                        }
                                    }
                                }

                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
