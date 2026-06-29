package com.xnihilfx.sirmobile.ui.candidates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.remote.dto.CreateCandidateRequest
import com.xnihilfx.sirmobile.data.remote.toUserMessage
import com.xnihilfx.sirmobile.data.repository.CandidatesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class NewCandidateUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val source: String = "",
    val saving: Boolean = false,
    val error: String? = null,
)

sealed interface NewCandidateEvent {
    data class Created(val id: Int) : NewCandidateEvent
    data class Error(val message: String) : NewCandidateEvent
}

@HiltViewModel
class NewCandidateViewModel @Inject constructor(
    private val repo: CandidatesRepository,
    private val json: Json,
) : ViewModel() {

    private val _state = MutableStateFlow(NewCandidateUiState())
    val state: StateFlow<NewCandidateUiState> = _state.asStateFlow()

    private val _events = Channel<NewCandidateEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onFirstName(v: String) = _state.update { it.copy(firstName = v) }
    fun onLastName(v: String) = _state.update { it.copy(lastName = v) }
    fun onPhone(v: String) = _state.update { it.copy(phone = v) }
    fun onEmail(v: String) = _state.update { it.copy(email = v) }
    fun onSource(v: String) = _state.update { it.copy(source = v) }

    fun submit() {
        val s = _state.value
        if (s.firstName.isBlank() || s.lastName.isBlank()) {
            _events.trySend(NewCandidateEvent.Error("Nombre y apellido son requeridos"))
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                repo.create(
                    CreateCandidateRequest(
                        firstName = s.firstName.trim(),
                        lastName = s.lastName.trim(),
                        phoneNumber = s.phone.trimOrNull(),
                        email = s.email.trimOrNull(),
                        source = s.source.trimOrNull(),
                    ),
                )
            }.onSuccess { candidate ->
                _state.update { it.copy(saving = false) }
                _events.trySend(NewCandidateEvent.Created(candidate.id))
            }.onFailure { e ->
                val msg = e.toUserMessage(json)
                _state.update { it.copy(saving = false, error = msg) }
                _events.trySend(NewCandidateEvent.Error(msg))
            }
        }
    }

    private fun String.trimOrNull() = trim().ifBlank { null }
}
