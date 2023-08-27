package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.DebugButtonAction
import com.ramitsuri.choresclient.model.LoginDebugViewState
import com.ramitsuri.choresclient.model.LoginViewState
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.repositories.PushMessageTokenRepository
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository,
    private val syncRepository: SyncRepository,
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val pushMessageTokenRepository: PushMessageTokenRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider,
    private val isDebug: Boolean
) : ViewModel() {
    private val _state: MutableStateFlow<LoginViewState> = MutableStateFlow(
        LoginViewState(
            isLoggedIn = prefManager.isLoggedIn(),
            loginDebugViewState = getDebugViewState()
        )
    )
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
                    syncRepository.refresh()
                    taskAssignmentsRepository.refresh()
                    _state.update {
                        it.copy(loading = false, isLoggedIn = true)
                    }
                    launch {
                        pushMessageTokenRepository.submitToken()
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