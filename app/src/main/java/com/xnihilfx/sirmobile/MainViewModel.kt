package com.xnihilfx.sirmobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xnihilfx.sirmobile.data.local.SessionStore
import com.xnihilfx.sirmobile.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val session: SessionStore) : ViewModel() {

    sealed interface StartState {
        data object Loading : StartState
        data class Ready(val route: String) : StartState
    }

    private val _state = MutableStateFlow<StartState>(StartState.Loading)
    val state: StateFlow<StartState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            session.load()
            _state.value = StartState.Ready(
                if (session.accessToken != null) Route.Opportunities.path else Route.Login.path
            )
        }
    }
}
