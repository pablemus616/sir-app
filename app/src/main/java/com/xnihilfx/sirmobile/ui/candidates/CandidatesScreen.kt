package com.xnihilfx.sirmobile.ui.candidates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import com.xnihilfx.sirmobile.ui.components.PressableCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Plus
import com.xnihilfx.sirmobile.data.remote.dto.CandidateDto
import com.xnihilfx.sirmobile.ui.components.EmptyView
import com.xnihilfx.sirmobile.ui.components.ErrorView
import com.xnihilfx.sirmobile.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidatesScreen(
    opportunityId: Int,
    onCandidateClick: (candId: Int) -> Unit,
    onNewCandidate: () -> Unit,
    onBack: () -> Unit,
    viewModel: CandidatesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var fabVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fabVisible = true
        viewModel.events.collect { event ->
            when (event) {
                is CandidatesEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidatos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = FeatherIcons.ArrowLeft,
                            contentDescription = "Regresar",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = scaleIn(tween(250)) + fadeIn(tween(250)),
                exit = scaleOut(tween(200)) + fadeOut(tween(200)),
            ) {
                FloatingActionButton(onClick = onNewCandidate) {
                    Icon(
                        imageVector = FeatherIcons.Plus,
                        contentDescription = "Nuevo candidato",
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de búsqueda
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQuery,
                    label = { Text("Buscar candidato") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtros de estado (opcional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.status == null,
                        onClick = { viewModel.onStatus(null) },
                        label = { Text("Todos") },
                    )
                    FilterChip(
                        selected = state.status == "active",
                        onClick = { viewModel.onStatus(if (state.status == "active") null else "active") },
                        label = { Text("Activos") },
                    )
                    FilterChip(
                        selected = state.status == "new",
                        onClick = { viewModel.onStatus(if (state.status == "new") null else "new") },
                        label = { Text("Nuevos") },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val contentKey = when {
                    state.loading -> "loading"
                    state.error != null && state.items.isEmpty() -> "error"
                    state.items.isEmpty() -> "empty"
                    else -> "content"
                }
                Crossfade(targetState = contentKey, label = "cand_content") { key ->
                    when (key) {
                        "loading" -> LoadingView()
                        "error" -> ErrorView(
                            message = state.error ?: "Error",
                            onRetry = viewModel::load,
                        )
                        "empty" -> EmptyView(text = "Sin candidatos encontrados")
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.items, key = { it.id }) { candidate ->
                                CandidateCard(
                                    candidate = candidate,
                                    onClick = { onCandidateClick(candidate.id) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) } // espacio para el FAB
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateCard(
    candidate: CandidateDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PressableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = candidate.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                candidate.phoneNumber?.let { phone ->
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                candidate.email?.let { email ->
                    if (candidate.phoneNumber != null) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Chip de estado
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = statusLabel(candidate.status),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}

private fun statusLabel(status: String): String = when (status) {
    "new" -> "Nuevo"
    "active" -> "Activo"
    "placed" -> "Colocado"
    "on_hold" -> "En espera"
    "discarded" -> "Descartado"
    else -> status
}
