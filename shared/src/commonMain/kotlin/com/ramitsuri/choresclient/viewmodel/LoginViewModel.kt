package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.LoginDebugViewState
import com.ramitsuri.choresclient.model.LoginViewState
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider,
    private val isDebug: Boolean
) : ViewModel() {
    private val _state: MutableStateFlow<LoginViewState> =
        if (prefManager.getKey().isNullOrEmpty() ||
            prefManager.getUserId().isNullOrEmpty() ||
            prefManager.getToken().isNullOrEmpty()
        ) {
            MutableStateFlow(
                LoginViewState(
                    isLoggedIn = false,
                    loginDebugViewState = getDebugViewState()
                )
            )
        } else {
            MutableStateFlow(
                LoginViewState(
                    isLoggedIn = true,
                    loginDebugViewState = getDebugViewState()
                )
            )
        }

    val state: StateFlow<LoginViewState> = _state

    fun login() {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch(dispatchers.main) {
            when (val loginResult = repository.login(_state.value.id, _state.value.key)) {
                is Result.Failure -> {
                    _state.update {
                        it.copy(loading = false, error = loginResult.error)
                    }
                }
                is Result.Success -> {
                    _state.update {
                        it.copy(loading = false, isLoggedIn = true)
                    }
                }
            }
        }
    }

    fun onIdUpdated(newId: String) {
        _state.update {
            it.copy(id = newId)
        }
    }

    fun onKeyUpdated(newKey: String) {
        _state.update {
            it.copy(key = newKey)
        }
    }

    fun onErrorShown() {
        _state.update {
            it.copy(error = null)
        }
    }


    fun setDebugServer(newValue: String) {
        _state.update {
            it.copy(loginDebugViewState = it.loginDebugViewState?.copy(serverText = newValue))
        }
        prefManager.setDebugServer(newValue)
    }

    fun getServer() = prefManager.getDebugServer()
    private fun getDebugViewState() = if (isDebug) {
        LoginDebugViewState(serverText = prefManager.getDebugServer())
    } else {
        null
    }
}