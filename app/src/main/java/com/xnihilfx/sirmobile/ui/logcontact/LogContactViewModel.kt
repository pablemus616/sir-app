package com.xnihilfx.sirmobile.ui.logcontact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.remote.dto.ContactTypeDto
import com.xnihilfx.sirmobile.data.remote.dto.CreateCandidateContactRequest
import com.xnihilfx.sirmobile.data.remote.toUserMessage
import com.xnihilfx.sirmobile.data.repository.CandidateContactsRepository
import com.xnihilfx.sirmobile.data.repository.ContactTypesRepository
import com.xnihilfx.sirmobile.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject

data class LogContactUiState(
    val loading: Boolean = false,
    val types: List<ContactTypeDto> = emptyList(),
    val selectedTypeId: Int? = null,
    val direction: String = "outbound",
    val notes: String = "",
    val callLength: Int? = null,
    val phoneDialed: String? = null,
    val saving: Boolean = false,
    val error: String? = null,
    /** Teléfono del candidato para los accesos rápidos (cargado desde ruta). */
    val candidatePhone: String? = null,
    /** Email del candidato para los accesos rápidos (cargado desde ruta). */
    val candidateEmail: String? = null,
    /** Número marcado en el atajo de llamada (para buscar en el call log al volver). */
    val pendingCallNumber: String? = null,
)

sealed interface LogContactEvent {
    data object Saved : LogContactEvent
    data class Error(val message: String) : LogContactEvent
}

@HiltViewModel
class LogContactViewModel @Inject constructor(
    private val contactsRepo: CandidateContactsRepository,
    private val contactTypesRepo: ContactTypesRepository,
    private val json: Json,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val candidateId: Int = savedStateHandle.get<Int>(Route.LogContact.ARG_CAND) ?: -1
    private val opportunityId: Int = savedStateHandle.get<Int>(Route.LogContact.ARG_OPP) ?: -1

    /**
     * Constructor secundario de prueba. Permite instanciar el ViewModel sin Hilt
     * pasando los IDs directamente (en lugar de via SavedStateHandle).
     */
    internal constructor(
        contactsRepo: CandidateContactsRepository,
        contactTypesRepo: ContactTypesRepository,
        json: Json,
        candidateId: Int,
        opportunityId: Int,
    ) : this(
        contactsRepo,
        contactTypesRepo,
        json,
        SavedStateHandle(
            mapOf(
                Route.LogContact.ARG_CAND to candidateId,
                Route.LogContact.ARG_OPP to opportunityId,
            ),
        ),
    )

    private val _state = MutableStateFlow(LogContactUiState())
    val state: StateFlow<LogContactUiState> = _state.asStateFlow()

    private val _events = Channel<LogContactEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Carga el teléfono/email del candidato desde los parámetros de la ruta (si los hay).
        val phone = savedStateHandle.get<String>(Route.LogContact.ARG_PHONE)
        val email = savedStateHandle.get<String>(Route.LogContact.ARG_EMAIL)
        _state.update { it.copy(candidatePhone = phone, candidateEmail = email) }

        loadTypes()
    }

    fun loadTypes() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            runCatching { contactTypesRepo.all() }
                .onSuccess { types ->
                    _state.update { it.copy(loading = false, types = types) }
                }
                .onFailure { e ->
                    val msg = e.toUserMessage(json)
                    _state.update { it.copy(loading = false, error = msg) }
                    _events.trySend(LogContactEvent.Error(msg))
                }
        }
    }

    fun onTypeSelected(typeId: Int) = _state.update { it.copy(selectedTypeId = typeId) }
    fun onDirection(dir: String) = _state.update { it.copy(direction = dir) }
    fun onNotes(notes: String) = _state.update { it.copy(notes = notes) }
    fun onCallLength(minutes: Int?) = _state.update { it.copy(callLength = minutes) }
    fun setCallLength(v: Int?) = _state.update { it.copy(callLength = v) }
    fun onPhoneDialed(phone: String?) = _state.update { it.copy(phoneDialed = phone) }

    /**
     * Atajo rápido de contacto. Selecciona automáticamente el tipo de contacto
     * cuyo nombre coincide con [kind] ("call", "whatsapp", "email") y configura
     * los campos relacionados.
     */
    fun pickShortcut(kind: String) {
        val type = _state.value.types.find { it.name == kind }
        _state.update { s ->
            when (kind) {
                "call" -> s.copy(
                    selectedTypeId = type?.id,
                    pendingCallNumber = s.candidatePhone,
                    phoneDialed = s.candidatePhone,
                    direction = "outbound",
                )
                "whatsapp" -> s.copy(
                    selectedTypeId = type?.id,
                    phoneDialed = s.candidatePhone,
                    direction = "outbound",
                )
                "email" -> s.copy(selectedTypeId = type?.id)
                else -> s.copy(selectedTypeId = type?.id)
            }
        }
    }

    /**
     * Llamado cuando el usuario regresa a la pantalla tras una llamada.
     * Si [durationSeconds] no es nulo, actualiza la duración automáticamente
     * desde el call log; de lo contrario deja el valor actual (para edición manual).
     */
    fun onReturnFromCall(durationSeconds: Int?) {
        if (durationSeconds != null) {
            _state.update { it.copy(callLength = durationSeconds) }
        }
    }

    fun submit() {
        val s = _state.value
        if (s.selectedTypeId == null) {
            _events.trySend(LogContactEvent.Error("Selecciona un tipo de contacto"))
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            runCatching {
                contactsRepo.create(
                    CreateCandidateContactRequest(
                        candidateId = candidateId,
                        opportunityId = opportunityId,
                        contactType = s.selectedTypeId,
                        contactTime = Instant.now().toString(),
                        direction = s.direction.ifBlank { null },
                        callLength = s.callLength,
                        contactDesc = s.notes.ifBlank { null },
                        phoneNumberDialed = s.phoneDialed?.ifBlank { null },
                    ),
                )
            }.onSuccess {
                _state.update { it.copy(saving = false) }
                _events.trySend(LogContactEvent.Saved)
            }.onFailure { e ->
                val msg = e.toUserMessage(json)
                _state.update { it.copy(saving = false, error = msg) }
                _events.trySend(LogContactEvent.Error(msg))
            }
        }
    }
}
