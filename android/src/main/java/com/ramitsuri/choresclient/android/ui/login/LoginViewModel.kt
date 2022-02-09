package com.ramitsuri.choresclient.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.choresclient.android.model.LoginViewState
import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.repositories.LoginRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
) : ViewModel() {
    private val _state: MutableLiveData<ViewState<LoginViewState>> =
        if (prefManager.getKey().isNullOrEmpty() ||
            prefManager.getUserId().isNullOrEmpty() ||
            prefManager.getToken().isNullOrEmpty()
        ) {
            Timber.i("${prefManager.getKey()}, ${prefManager.getUserId()}, ${prefManager.getToken()}")
            MutableLiveData((ViewState.Login))
        } else {
            MutableLiveData(ViewState.Success(LoginViewState(true)))
        }

    val state: LiveData<ViewState<LoginViewState>> = _state

    fun login(id: String, key: String) {
        _state.value = ViewState.Loading
        viewModelScope.launch(dispatchers.main) {
            when (val loginResult = repository.login(id, key)) {
                is Result.Failure -> {
                    _state.value = ViewState.Error(loginResult.error)
                }
                is Result.Success -> {
                    _state.value = ViewState.Success(LoginViewState(true))
                }
            }
        }
    }

    fun setDebugServer(server: String) {
        prefManager.setDebugServer(server)
    }

    fun getServer() = prefManager.getDebugServer()
}