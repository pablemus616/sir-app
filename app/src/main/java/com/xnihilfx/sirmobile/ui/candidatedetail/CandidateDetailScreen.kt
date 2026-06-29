package com.xnihilfx.sirmobile.ui.candidatedetail

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import com.xnihilfx.sirmobile.data.remote.dto.CandidateContactDto
import com.xnihilfx.sirmobile.data.remote.dto.CandidateDto
import com.xnihilfx.sirmobile.ui.components.EmptyView
import com.xnihilfx.sirmobile.ui.components.ErrorView
import com.xnihilfx.sirmobile.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateDetailScreen(
    candidateId: Int,
    opportunityId: Int,
    onLogContact: (candidateId: Int, opportunityId: Int) -> Unit,
    onMoveStage: (candidateId: Int, opportunityId: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: CandidateDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Recargar al entrar y al regresar de Registrar Contacto
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.load()
        }
    }

    // Eventos de un solo disparo (errores)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CandidateDetailEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.candidate?.fullName ?: "Candidato") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(paddingValues),
        ) {
            when {
                state.loading && state.candidate == null -> {
                    LoadingView()
                }

                state.error != null && state.candidate == null -> {
                    ErrorView(
                        message = state.error!!,
                        onRetry = viewModel::load,
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                    // Encabezado del candidato
                    item {
                        state.candidate?.let { candidate ->
                            CandidateHeader(candidate = candidate)
                        }
                    }

                    // Botones de acción
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { onLogContact(candidateId, opportunityId) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Registrar contacto")
                            }
                            OutlinedButton(
                                onClick = { onMoveStage(candidateId, opportunityId) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Mover etapa")
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Historial de contactos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (state.contacts.isEmpty() && !state.loading) {
                        item {
                            EmptyView(
                                text = "Sin contactos aún",
                                modifier = Modifier.height(120.dp),
                            )
                        }
                    } else {
                        items(state.contacts, key = { it.id }) { contact ->
                            ContactHistoryRow(contact = contact)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
        }
    }
}

@Composable
private fun CandidateHeader(candidate: CandidateDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = candidate.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            candidate.headline?.let { headline ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                candidate.phoneNumber?.let { phone ->
                    Column {
                        Text(
                            text = "Teléfono",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                candidate.email?.let { email ->
                    Column {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = candidateStatusLabel(candidate.status),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}

@Composable
private fun ContactHistoryRow(contact: CandidateContactDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = contactTypeLabel(contact.contactType?.name),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = relativeTime(contact.contactTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            contact.direction?.let { dir ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = directionLabel(dir),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            contact.recruiter?.let { recruiter ->
                Spacer(modifier = Modifier.height(2.dp))
                val recruiterName = listOfNotNull(recruiter.firstName, recruiter.lastName).joinToString(" ")
                Text(
                    text = "Por: $recruiterName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            contact.contactDesc?.let { desc ->
                if (desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun candidateStatusLabel(status: String): String = when (status) {
    "new" -> "Nuevo"
    "active" -> "Activo"
    "placed" -> "Colocado"
    "on_hold" -> "En espera"
    "discarded" -> "Descartado"
    else -> status
}

private fun contactTypeLabel(name: String?): String = when (name) {
    "call" -> "Llamada"
    "email" -> "Email"
    "meeting" -> "Reunión"
    "whatsapp" -> "WhatsApp"
    else -> name ?: "Contacto"
}

private fun directionLabel(direction: String): String = when (direction) {
    "inbound" -> "Entrante"
    "outbound" -> "Saliente"
    else -> direction
}

private fun relativeTime(isoTime: String): String {
    return try {
        val instant = java.time.Instant.parse(isoTime)
        val now = java.time.Instant.now()
        val diffSeconds = java.time.Duration.between(instant, now).seconds
        when {
            diffSeconds < 60 -> "Hace un momento"
            diffSeconds < 3600 -> "Hace ${diffSeconds / 60} min"
            diffSeconds < 86400 -> "Hace ${diffSeconds / 3600} h"
            diffSeconds < 604800 -> "Hace ${diffSeconds / 86400} días"
            else -> java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy")
                .withZone(java.time.ZoneId.systemDefault())
                .format(instant)
        }
    } catch (e: Exception) {
        isoTime
    }
}
