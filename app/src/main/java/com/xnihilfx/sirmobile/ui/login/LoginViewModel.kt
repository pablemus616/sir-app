package com.xnihilfx.sirmobile.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.remote.toUserMessage
import com.xnihilfx.sirmobile.data.repository.AuthRepository
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

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
)

sealed interface LoginEvent {
    data object Success : LoginEvent
    data class Error(val message: String) : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val json: Json,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onUsername(v: String) = _state.update { it.copy(username = v) }
    fun onPassword(v: String) = _state.update { it.copy(password = v) }

    fun submit() {
        val s = _state.value
        if (s.username.isBlank() || s.password.isBlank()) {
            _events.trySend(LoginEvent.Error("Ingresa usuario y contraseña"))
            return
        }
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            runCatching { auth.login(s.username, s.password) }
                .onSuccess {
                    _state.update { it.copy(loading = false) }
                    _events.trySend(LoginEvent.Success)
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false) }
                    _events.trySend(LoginEvent.Error(e.toUserMessage(json)))
                }
        }
    }
}
