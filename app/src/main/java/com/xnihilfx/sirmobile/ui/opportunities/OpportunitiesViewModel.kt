package com.xnihilfx.sirmobile.ui.opportunities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.remote.dto.OpportunityDto
import com.xnihilfx.sirmobile.data.remote.toUserMessage
import com.xnihilfx.sirmobile.data.repository.AuthRepository
import com.xnihilfx.sirmobile.data.repository.OpportunitiesRepository
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

data class OpportunitiesUiState(
    val loading: Boolean = false,
    val items: List<OpportunityDto> = emptyList(),
    val mineOnly: Boolean = false,
    val query: String = "",
    val error: String? = null,
) {
    val filteredItems: List<OpportunityDto>
        get() = if (query.isBlank()) items
        else items.filter { opp ->
            val q = query.trim().lowercase()
            opp.title?.lowercase()?.contains(q) == true ||
                opp.client?.name?.lowercase()?.contains(q) == true
        }
}

sealed interface OpportunitiesEvent {
    data class Error(val message: String) : OpportunitiesEvent
    data object LoggedOut : OpportunitiesEvent
}

@HiltViewModel
class OpportunitiesViewModel @Inject constructor(
    private val repo: OpportunitiesRepository,
    private val auth: AuthRepository,
    private val json: Json,
) : ViewModel() {

    private val _state = MutableStateFlow(OpportunitiesUiState())
    val state: StateFlow<OpportunitiesUiState> = _state.asStateFlow()

    private val _events = Channel<OpportunitiesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    fun load() {
        val mineOnly = _state.value.mineOnly
        val employeeId = auth.employeeId()
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { repo.openOpportunities(mineOnly, employeeId) }
                .onSuccess { items ->
                    _state.update { it.copy(loading = false, items = items) }
                }
                .onFailure { e ->
                    val msg = e.toUserMessage(json)
                    _state.update { it.copy(loading = false, error = msg) }
                    _events.trySend(OpportunitiesEvent.Error(msg))
                }
        }
    }

    fun onQuery(q: String) = _state.update { it.copy(query = q) }

    fun onMineOnly(value: Boolean) {
        _state.update { it.copy(mineOnly = value) }
        load()
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { auth.logout() }
            _events.trySend(OpportunitiesEvent.LoggedOut)
        }
    }
}
