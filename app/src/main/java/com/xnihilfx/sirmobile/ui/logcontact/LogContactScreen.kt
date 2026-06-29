package com.xnihilfx.sirmobile.ui.logcontact

import android.Manifest
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import com.xnihilfx.sirmobile.ui.components.ErrorView
import com.xnihilfx.sirmobile.ui.components.LoadingView
import com.xnihilfx.sirmobile.util.CallLogReader
import com.xnihilfx.sirmobile.util.ContactIntents

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LogContactScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: LogContactViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Permiso de lectura del log de llamadas (solicitado en tiempo de ejecución).
    val callLogPermission = rememberPermissionState(Manifest.permission.READ_CALL_LOG)

    // Bandera transitoria: indica que el usuario marcó y está "en llamada".
    var callInProgress by remember { mutableStateOf(false) }

    // Al volver a la pantalla (ON_RESUME) tras una llamada, intentamos leer la duración.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (callInProgress) {
            callInProgress = false
            if (callLogPermission.status.isGranted) {
                val duration = CallLogReader.lastCallDuration(context, state.pendingCallNumber)
                viewModel.onReturnFromCall(duration)
            }
        }
    }

    // Eventos de un solo disparo
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LogContactEvent.Saved -> onSaved()
                is LogContactEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar contacto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Regresar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            state.loading && state.types.isEmpty() -> LoadingView(modifier = Modifier.padding(paddingValues))
            state.error != null && state.types.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = viewModel::loadTypes,
                modifier = Modifier.padding(paddingValues),
            )
            else -> {
                val callType = state.types.find { it.name == "call" }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Tipo de contacto
                    item {
                        Column {
                            Text(
                                text = "Tipo de contacto",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            ) {
                                state.types.forEach { type ->
                                    FilterChip(
                                        selected = state.selectedTypeId == type.id,
                                        onClick = { viewModel.onTypeSelected(type.id) },
                                        label = { Text(contactTypeLabel(type.name)) },
                                    )
                                }
                            }
                        }
                    }

                    // Dirección (entrante / saliente)
                    item {
                        Column {
                            Text(
                                text = "Dirección",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = state.direction == "outbound",
                                    onClick = { viewModel.onDirection("outbound") },
                                    label = { Text("Saliente") },
                                )
                                FilterChip(
                                    selected = state.direction == "inbound",
                                    onClick = { viewModel.onDirection("inbound") },
                                    label = { Text("Entrante") },
                                )
                            }
                        }
                    }

                    // Duración de la llamada en segundos (solo si el tipo es "call")
                    if (state.selectedTypeId == callType?.id) {
                        item {
                            OutlinedTextField(
                                value = state.callLength?.toString() ?: "",
                                onValueChange = { v -> viewModel.setCallLength(v.toIntOrNull()) },
                                label = { Text("Duración (s)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                supportingText = {
                                    Text("Autocompletado desde el registro de llamadas; editable manualmente.")
                                },
                            )
                        }
                    }

                    // Notas
                    item {
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = viewModel::onNotes,
                            label = { Text("Notas") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                        )
                    }

                    // Accesos rápidos (Llamar / WhatsApp / Email)
                    val phone = state.candidatePhone
                    val email = state.candidateEmail
                    if (phone != null || email != null) {
                        item {
                            Column {
                                Text(
                                    text = "Accesos rápidos",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (phone != null) {
                                        OutlinedButton(onClick = {
                                            viewModel.pickShortcut("call")
                                            ContactIntents.dial(context, phone)
                                            // Solicitar permiso READ_CALL_LOG para capturar duración al volver.
                                            if (!callLogPermission.status.isGranted) {
                                                callLogPermission.launchPermissionRequest()
                                            }
                                            callInProgress = true
                                        }) { Text("Llamar") }

                                        OutlinedButton(onClick = {
                                            viewModel.pickShortcut("whatsapp")
                                            ContactIntents.whatsapp(context, phone)
                                        }) { Text("WhatsApp") }
                                    }
                                    if (email != null) {
                                        OutlinedButton(onClick = {
                                            viewModel.pickShortcut("email")
                                            ContactIntents.email(context, email)
                                        }) { Text("Email") }
                                    }
                                }
                            }
                        }
                    }

                    // Botón Guardar
                    item {
                        Button(
                            onClick = viewModel::submit,
                            enabled = !state.saving,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (state.saving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Guardar")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

private fun contactTypeLabel(name: String): String = when (name) {
    "call" -> "Llamada"
    "email" -> "Email"
    "meeting" -> "Reunión"
    "whatsapp" -> "WhatsApp"
    else -> name
}
