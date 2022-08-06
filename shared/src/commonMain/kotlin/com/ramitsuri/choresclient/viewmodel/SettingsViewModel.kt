package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.SettingsViewState
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val syncRepository: SyncRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
) : ViewModel(), KoinComponent {

    private val _state =
        MutableStateFlow(
            SettingsViewState(
                lastSyncTime = prefManager.getLastSyncTime(),
                timeZone = TimeZone.currentSystemDefault()
            )
        )
    val state: StateFlow<SettingsViewState> = _state

    fun syncRequested() {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch(dispatchers.io) {
            when (val syncResult = syncRepository.refresh()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(loading = false, lastSyncTime = prefManager.getLastSyncTime())
                    }
                }
                is Result.Failure -> {
                    _state.update {
                        it.copy(loading = false, error = syncResult.error)
                    }
                }
            }
        }
    }

    fun onErrorShown() {
        _state.update {
            it.copy(error = null)
        }
    }
}