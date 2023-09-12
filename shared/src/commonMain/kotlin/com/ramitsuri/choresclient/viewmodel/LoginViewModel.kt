package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.view.DebugButtonAction
import com.ramitsuri.choresclient.model.view.LoginDebugViewState
import com.ramitsuri.choresclient.model.view.LoginViewState
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository,
    private val contentDownloadRequestHandler: ContentDownloadRequestHandler,
    private val prefManager: PrefManager,
    private val isDebug: Boolean
) : ViewModel() {
    private val _state: MutableStateFlow<LoginViewState> = MutableStateFlow(
        LoginViewState(
            loginDebugViewState = getDebugViewState()
        )
    )
    val state: StateFlow<LoginViewState> = _state

    private val _isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun login() {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            when (val loginResult = repository.login(_state.value.id, _state.value.key)) {
                is Result.Failure -> {
                    _state.update {
                        it.copy(loading = false, error = loginResult.error)
                    }
                }

                is Result.Success -> {
                    contentDownloadRequestHandler.requestImmediateDownload().collect { completed ->
                        if (completed) {
                            setLoggedIn()
                            _state.update {
                                it.copy(loading = false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun onIdUpdated(newId: String) {
        _state.update {
            it.copy(id = newId, allowLogin = newId.isNotEmpty() && it.key.isNotEmpty())
        }
    }

    fun onKeyUpdated(newKey: String) {
        _state.update {
            it.copy(key = newKey, allowLogin = it.id.isNotEmpty() && newKey.isNotEmpty())
        }
    }

    fun onServerUrlUpdated(url: String) {
        _state.update { previousState ->
            previousState.copy(
                loginDebugViewState =
                previousState.loginDebugViewState?.copy(
                    serverUrl = url,
                )
            )
        }
    }

    fun onErrorShown() {
        _state.update {
            it.copy(error = null)
        }
    }


    fun setDebugServer() {
        _state.update { previousState ->
            previousState.copy(
                loginDebugViewState = previousState.loginDebugViewState?.copy(
                    debugButtonAction = DebugButtonAction.RESTART
                )
            )
        }
        prefManager.setDebugServer(_state.value.loginDebugViewState?.serverUrl ?: "")
    }

    fun resetDebugServer() {
        _state.update { previousState ->
            previousState.copy(
                loginDebugViewState = getDebugViewState()
            )
        }
    }

    private fun setLoggedIn() {
        _isLoggedIn.update {
            true
        }
    }

    private fun getDebugViewState() = if (isDebug) {
        val savedDebugServer = prefManager.getDebugServer()
        LoginDebugViewState(
            serverUrl = savedDebugServer.ifEmpty { "http://" },
            debugButtonAction = if (savedDebugServer.isEmpty()) {
                DebugButtonAction.SET_SERVER
            } else {
                DebugButtonAction.UPDATE_SERVER
            }
        )
    } else {
        null
    }
}