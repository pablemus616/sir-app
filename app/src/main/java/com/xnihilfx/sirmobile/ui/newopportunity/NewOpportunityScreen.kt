package com.xnihilfx.sirmobile.ui.newopportunity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Minus
import compose.icons.feathericons.Plus
import com.xnihilfx.sirmobile.ui.components.EmptyView
import com.xnihilfx.sirmobile.ui.components.ErrorView
import com.xnihilfx.sirmobile.ui.components.LoadingView

private val seniorityOptions = listOf("junior", "mid", "senior", "lead")
private val seniorityLabels = mapOf(
    "junior" to "Junior",
    "mid" to "Mid",
    "senior" to "Senior",
    "lead" to "Lead",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewOpportunityScreen(
    onCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: NewOpportunityViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NewOpportunityEvent.Created -> onCreated()
                is NewOpportunityEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva vacante") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = FeatherIcons.ArrowLeft, contentDescription = "Regresar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            state.loading -> LoadingView(modifier = Modifier.padding(paddingValues))
            state.error != null && state.clients.isEmpty() -> ErrorView(
                message = state.error!!,
                onRetry = {},
                modifier = Modifier.padding(paddingValues),
            )
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cliente (required)
                    ClientDropdown(
                        clients = state.clients,
                        selectedClientId = state.clientId,
                        onClientSelected = viewModel::onClientId,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Título (optional)
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitle,
                        label = { Text("Título (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Área (optional)
                    if (state.areas.isNotEmpty()) {
                        AreaDropdown(
                            areas = state.areas,
                            selectedAreaId = state.areaId,
                            onAreaSelected = viewModel::onAreaId,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Headcount stepper
                    Text(
                        text = "Vacantes",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(
                            onClick = { viewModel.onHeadcount(state.headcount - 1) },
                            enabled = state.headcount > 1,
                        ) {
                            Icon(imageVector = FeatherIcons.Minus, contentDescription = "Reducir")
                        }
                        Text(
                            text = state.headcount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        IconButton(onClick = { viewModel.onHeadcount(state.headcount + 1) }) {
                            Icon(imageVector = FeatherIcons.Plus, contentDescription = "Aumentar")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Seniority chips (optional)
                    Text(
                        text = "Seniority (opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        seniorityOptions.forEach { option ->
                            FilterChip(
                                selected = state.seniority == option,
                                onClick = {
                                    viewModel.onSeniority(if (state.seniority == option) null else option)
                                },
                                label = { Text(seniorityLabels[option] ?: option) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.saving) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = viewModel::submit,
                            enabled = state.clientId != null,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Guardar vacante")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDropdown(
    clients: List<com.xnihilfx.sirmobile.data.remote.dto.ClientDto>,
    selectedClientId: Int?,
    onClientSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = clients.firstOrNull { it.id == selectedClientId }?.name ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Cliente *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            clients.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.name) },
                    onClick = {
                        onClientSelected(client.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AreaDropdown(
    areas: List<com.xnihilfx.sirmobile.data.remote.dto.AreaDto>,
    selectedAreaId: Int?,
    onAreaSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = areas.firstOrNull { it.id == selectedAreaId }?.name ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Área (opcional)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Sin área") },
                onClick = {
                    onAreaSelected(null)
                    expanded = false
                },
            )
            areas.forEach { area ->
                DropdownMenuItem(
                    text = { Text(area.name) },
                    onClick = {
                        onAreaSelected(area.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
