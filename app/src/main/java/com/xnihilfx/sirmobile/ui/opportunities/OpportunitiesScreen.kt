package com.xnihilfx.sirmobile.ui.opportunities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import compose.icons.FeatherIcons
import compose.icons.feathericons.LogOut
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xnihilfx.sirmobile.data.remote.dto.OpportunityDto
import com.xnihilfx.sirmobile.ui.components.EmptyView
import com.xnihilfx.sirmobile.ui.components.ErrorView
import com.xnihilfx.sirmobile.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunitiesScreen(
    onOpportunityClick: (oppId: Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: OpportunitiesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OpportunitiesEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is OpportunitiesEvent.LoggedOut -> onLogout()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puestos") },
                actions = {
                    IconButton(onClick = viewModel::logout) {
                        Icon(
                            imageVector = FeatherIcons.LogOut,
                            contentDescription = "Cerrar sesión",
                        )
                    }
                },
            )
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

                // Filtro "mis puestos"
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = state.mineOnly,
                        onClick = { viewModel.onMineOnly(!state.mineOnly) },
                        label = { Text("Mis puestos") },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de búsqueda
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQuery,
                    label = { Text("Buscar por puesto o cliente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    state.loading -> LoadingView()
                    state.error != null && state.items.isEmpty() -> ErrorView(
                        message = state.error!!,
                        onRetry = viewModel::load,
                    )
                    state.filteredItems.isEmpty() -> EmptyView(text = "Sin puestos abiertos")
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.filteredItems, key = { it.id }) { opp ->
                                OpportunityCard(
                                    opportunity = opp,
                                    onClick = { onOpportunityClick(opp.id) },
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpportunityCard(
    opportunity: OpportunityDto,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = opportunity.title ?: "Vacante #${opportunity.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            opportunity.client?.let { client ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                opportunity.area?.let { area ->
                    Text(
                        text = area.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                opportunity.pipelineStage?.let { stage ->
                    Text(
                        text = stage.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = "${opportunity.headcount} vacante(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
