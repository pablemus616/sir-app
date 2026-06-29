package com.xnihilfx.sirmobile.ui.candidatedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.remote.dto.CandidateContactDto
import com.xnihilfx.sirmobile.data.remote.dto.CandidateDto
import com.xnihilfx.sirmobile.data.remote.toUserMessage
import com.xnihilfx.sirmobile.data.repository.CandidateContactsRepository
import com.xnihilfx.sirmobile.data.repository.CandidatesRepository
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
import javax.inject.Inject

data class CandidateDetailUiState(
    val loading: Boolean = false,
    val candidate: CandidateDto? = null,
    val contacts: List<CandidateContactDto> = emptyList(),
    val error: String? = null,
)

sealed interface CandidateDetailEvent {
    data class Error(val message: String) : CandidateDetailEvent
}

@HiltViewModel
class CandidateDetailViewModel @Inject constructor(
    private val candidatesRepo: CandidatesRepository,
    private val contactsRepo: CandidateContactsRepository,
    private val json: Json,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val candidateId: Int = savedStateHandle.get<Int>(Route.CandidateDetail.ARG_CAND) ?: -1
    val opportunityId: Int = savedStateHandle.get<Int>(Route.CandidateDetail.ARG_OPP) ?: -1

    private val _state = MutableStateFlow(CandidateDetailUiState())
    val state: StateFlow<CandidateDetailUiState> = _state.asStateFlow()

    private val _events = Channel<CandidateDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * Carga el candidato y su historial de contactos.
     * Se llama desde la pantalla cada vez que entra en estado STARTED
     * (primer acceso y al regresar de Registrar Contacto).
     */
    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val candidate = candidatesRepo.get(candidateId)
                val contacts = contactsRepo.historyFor(candidateId)
                candidate to contacts
            }.onSuccess { (candidate, contacts) ->
                _state.update { it.copy(loading = false, candidate = candidate, contacts = contacts) }
            }.onFailure { e ->
                val msg = e.toUserMessage(json)
                _state.update { it.copy(loading = false, error = msg) }
                _events.trySend(CandidateDetailEvent.Error(msg))
            }
        }
    }
}
