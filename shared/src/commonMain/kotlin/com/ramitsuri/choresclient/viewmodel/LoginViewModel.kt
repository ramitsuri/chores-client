package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.LoginViewState
import com.ramitsuri.choresclient.model.ViewEvent
import com.ramitsuri.choresclient.model.ViewState
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
) : ViewModel() {
    private val _state: MutableStateFlow<ViewState<LoginViewState>> =
        if (prefManager.getKey().isNullOrEmpty() ||
            prefManager.getUserId().isNullOrEmpty() ||
            prefManager.getToken().isNullOrEmpty()
        ) {
            MutableStateFlow((ViewState.Event(ViewEvent.LOGIN)))
        } else {
            MutableStateFlow(ViewState.Success(LoginViewState(true)))
        }

    val state: StateFlow<ViewState<LoginViewState>> = _state

    fun login(id: String, key: String) {
        _state.update {
            ViewState.Event(ViewEvent.LOADING)
        }
        viewModelScope.launch(dispatchers.main) {
            when (val loginResult = repository.login(id, key)) {
                is Result.Failure -> {
                    _state.update {
                        ViewState.Error(loginResult.error)
                    }
                }
                is Result.Success -> {
                    _state.update {
                        ViewState.Success(LoginViewState(true))
                    }
                }
            }
        }
    }

    fun setDebugServer(server: String) {
        prefManager.setDebugServer(server)
    }

    fun getServer() = prefManager.getDebugServer()
}