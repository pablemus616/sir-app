package com.xnihilfx.sirmobile.ui.candidates

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.remote.dto.CandidateDto
import com.xnihilfx.sirmobile.data.remote.toUserMessage
import com.xnihilfx.sirmobile.data.repository.CandidatesRepository
import com.xnihilfx.sirmobile.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class CandidatesUiState(
    val loading: Boolean = false,
    val query: String = "",
    val status: String? = null,
    val items: List<CandidateDto> = emptyList(),
    val error: String? = null,
)

sealed interface CandidatesEvent {
    data class Error(val message: String) : CandidatesEvent
}

@HiltViewModel
class CandidatesViewModel @Inject constructor(
    private val repo: CandidatesRepository,
    private val json: Json,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // opportunityId recibido desde el argumento de navegación
    val opportunityId: Int = savedStateHandle.get<Int>(Route.Candidates.ARG_OPP) ?: -1

    private val _state = MutableStateFlow(CandidatesUiState())
    val state: StateFlow<CandidatesUiState> = _state.asStateFlow()

    private val _events = Channel<CandidatesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Flow interno para la búsqueda debounced
    private val _queryFlow = MutableStateFlow("")

    init {
        load()
        observeQueryChanges()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeQueryChanges() {
        viewModelScope.launch {
            _queryFlow
                .drop(1) // omitir el valor inicial "" ya cubierto por load()
                .debounce(300)
                .flatMapLatest { query ->
                    flow {
                        _state.update { it.copy(loading = true, error = null) }
                        emit(runCatching {
                            repo.search(
                                name = query.takeIf { it.isNotBlank() },
                                status = _state.value.status,
                            )
                        })
                    }
                }
                .collect { result ->
                    result.onSuccess { items ->
                        _state.update { it.copy(loading = false, items = items) }
                    }.onFailure { e ->
                        val msg = e.toUserMessage(json)
                        _state.update { it.copy(loading = false, error = msg) }
                        _events.trySend(CandidatesEvent.Error(msg))
                    }
                }
        }
    }

    fun onQuery(q: String) {
        _state.update { it.copy(query = q) }
        _queryFlow.value = q
    }

    fun onStatus(status: String?) {
        _state.update { it.copy(status = status) }
        load()
    }

    fun load() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                repo.search(
                    name = current.query.takeIf { it.isNotBlank() },
                    status = current.status,
                )
            }.onSuccess { items ->
                _state.update { it.copy(loading = false, items = items) }
            }.onFailure { e ->
                val msg = e.toUserMessage(json)
                _state.update { it.copy(loading = false, error = msg) }
                _events.trySend(CandidatesEvent.Error(msg))
            }
        }
    }
}
