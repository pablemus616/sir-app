package com.xnihilfx.sirmobile.ui.newopportunity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.remote.dto.AreaDto
import com.xnihilfx.sirmobile.data.remote.dto.ClientDto
import com.xnihilfx.sirmobile.data.remote.dto.CreateOpportunityRequest
import com.xnihilfx.sirmobile.data.remote.dto.PipelineStageDto
import com.xnihilfx.sirmobile.data.remote.toUserMessage
import com.xnihilfx.sirmobile.data.repository.AuthRepository
import com.xnihilfx.sirmobile.data.repository.CatalogRepository
import com.xnihilfx.sirmobile.data.repository.OpportunitiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class NewOpportunityUiState(
    val loading: Boolean = false,
    val clients: List<ClientDto> = emptyList(),
    val areas: List<AreaDto> = emptyList(),
    val stages: List<PipelineStageDto> = emptyList(),
    val clientId: Int? = null,
    val title: String = "",
    val areaId: Int? = null,
    val headcount: Int = 1,
    val seniority: String? = null,
    val saving: Boolean = false,
    val error: String? = null,
) {
    val selectedClient: ClientDto? get() = clients.firstOrNull { it.id == clientId }
    val selectedArea: AreaDto? get() = areas.firstOrNull { it.id == areaId }
}

sealed interface NewOpportunityEvent {
    data class Created(val id: Int) : NewOpportunityEvent
    data class Error(val message: String) : NewOpportunityEvent
}

@HiltViewModel
class NewOpportunityViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val repo: OpportunitiesRepository,
    private val auth: AuthRepository,
    private val json: Json,
) : ViewModel() {

    private val _state = MutableStateFlow(NewOpportunityUiState())
    val state: StateFlow<NewOpportunityUiState> = _state.asStateFlow()

    private val _events = Channel<NewOpportunityEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { loadCatalogs() }

    fun loadCatalogs() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val clientsDeferred = async { catalog.clients() }
                val areasDeferred = async { catalog.areas() }
                val stagesDeferred = async { catalog.stages() }
                Triple(clientsDeferred.await(), areasDeferred.await(), stagesDeferred.await())
            }.onSuccess { (clients, areas, stages) ->
                _state.update { it.copy(loading = false, clients = clients, areas = areas, stages = stages) }
            }.onFailure { e ->
                val msg = e.toUserMessage(json)
                _state.update { it.copy(loading = false, error = msg) }
                _events.trySend(NewOpportunityEvent.Error(msg))
            }
        }
    }

    fun onClientId(id: Int?) = _state.update { it.copy(clientId = id) }
    fun onTitle(v: String) = _state.update { it.copy(title = v) }
    fun onAreaId(id: Int?) = _state.update { it.copy(areaId = id) }
    fun onHeadcount(v: Int) = _state.update { it.copy(headcount = v.coerceAtLeast(1)) }
    fun onSeniority(v: String?) = _state.update { it.copy(seniority = v) }

    fun submit() {
        val s = _state.value
        if (s.clientId == null) {
            _events.trySend(NewOpportunityEvent.Error("Selecciona un cliente"))
            return
        }
        val employeeId = auth.employeeId() ?: run {
            _events.trySend(NewOpportunityEvent.Error("Sesión no válida. Inicia sesión de nuevo."))
            return
        }
        val firstStageId = s.stages.minByOrNull { it.sortOrder }?.id ?: run {
            _events.trySend(NewOpportunityEvent.Error("No hay etapas de pipeline disponibles"))
            return
        }

        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                repo.create(
                    CreateOpportunityRequest(
                        clientId = s.clientId,
                        responsibleEmployeeId = employeeId,
                        pipelineStageId = firstStageId,
                        title = s.title.trimOrNull(),
                        areaId = s.areaId,
                        headcount = s.headcount.takeIf { it >= 1 },
                        seniority = s.seniority,
                    ),
                )
            }.onSuccess { opp ->
                _state.update { it.copy(saving = false) }
                _events.trySend(NewOpportunityEvent.Created(opp.id))
            }.onFailure { e ->
                val msg = e.toUserMessage(json)
                _state.update { it.copy(saving = false, error = msg) }
                _events.trySend(NewOpportunityEvent.Error(msg))
            }
        }
    }

    private fun String.trimOrNull() = trim().ifBlank { null }
}
