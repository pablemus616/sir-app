package com.xnihilfx.sirmobile.ui.movestage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.remote.dto.ApplicationDto
import com.xnihilfx.sirmobile.data.remote.toUserMessage
import com.xnihilfx.sirmobile.data.repository.ApplicationsRepository
import com.xnihilfx.sirmobile.domain.ApplicationStages
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

data class MoveStageUiState(
    val loading: Boolean = false,
    val application: ApplicationDto? = null,
    val legalNext: List<String> = emptyList(),
    val saving: Boolean = false,
    val error: String? = null,
)

sealed interface MoveStageEvent {
    data object Moved : MoveStageEvent
    data class Error(val message: String) : MoveStageEvent
}

@HiltViewModel
class MoveStageViewModel @Inject constructor(
    private val repo: ApplicationsRepository,
    private val json: Json,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val candidateId: Int = savedStateHandle.get<Int>(Route.MoveStage.ARG_CAND) ?: -1
    private val opportunityId: Int = savedStateHandle.get<Int>(Route.MoveStage.ARG_OPP) ?: -1

    /**
     * Constructor secundario de prueba — permite instanciar el ViewModel sin Hilt
     * pasando los IDs directamente.
     */
    internal constructor(
        repo: ApplicationsRepository,
        json: Json,
        candidateId: Int,
        opportunityId: Int,
    ) : this(
        repo,
        json,
        SavedStateHandle(
            mapOf(
                Route.MoveStage.ARG_CAND to candidateId,
                Route.MoveStage.ARG_OPP to opportunityId,
            ),
        ),
    )

    private val _state = MutableStateFlow(MoveStageUiState())
    val state: StateFlow<MoveStageUiState> = _state.asStateFlow()

    private val _events = Channel<MoveStageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repo.findFor(candidateId, opportunityId) }
                .onSuccess { app ->
                    val legal = app?.let { ApplicationStages.nextLegal(it.stage) } ?: emptyList()
                    _state.update { it.copy(loading = false, application = app, legalNext = legal) }
                }
                .onFailure { e ->
                    val msg = e.toUserMessage(json)
                    _state.update { it.copy(loading = false, error = msg) }
                    _events.trySend(MoveStageEvent.Error(msg))
                }
        }
    }

    /** Crea una aplicación nueva con etapa "applied" si aún no existe ninguna. */
    fun createApplication() {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching { repo.create(candidateId, opportunityId) }
                .onSuccess { app ->
                    val legal = ApplicationStages.nextLegal(app.stage)
                    _state.update { it.copy(saving = false, application = app, legalNext = legal) }
                }
                .onFailure { e ->
                    val msg = e.toUserMessage(json)
                    _state.update { it.copy(saving = false, error = msg) }
                    _events.trySend(MoveStageEvent.Error(msg))
                }
        }
    }

    /** Mueve la etapa de la aplicación actual a [stage]. */
    fun move(stage: String) {
        val appId = _state.value.application?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching { repo.changeStage(appId, stage) }
                .onSuccess { updated ->
                    val legal = ApplicationStages.nextLegal(updated.stage)
                    _state.update { it.copy(saving = false, application = updated, legalNext = legal) }
                    _events.trySend(MoveStageEvent.Moved)
                }
                .onFailure { e ->
                    val msg = e.toUserMessage(json)
                    _state.update { it.copy(saving = false, error = msg) }
                    _events.trySend(MoveStageEvent.Error(msg))
                }
        }
    }
}
