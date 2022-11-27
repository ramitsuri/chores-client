package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterItem
import com.ramitsuri.choresclient.model.FilterType
import com.ramitsuri.choresclient.model.NotificationActionWrapper
import com.ramitsuri.choresclient.model.NotificationActionsViewState
import com.ramitsuri.choresclient.model.SettingsViewState
import com.ramitsuri.choresclient.model.SyncViewState
import com.ramitsuri.choresclient.model.TextValue
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.resources.LocalizedString
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.FilterHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val syncRepository: SyncRepository,
    private val filterHelper: FilterHelper,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
) : ViewModel(), KoinComponent {

    private val _state =
        MutableStateFlow(
            SettingsViewState(
                syncViewState = SyncViewState(lastSyncTime = prefManager.getLastSyncTime()),
                timeZone = TimeZone.currentSystemDefault(),
                notificationActionsViewState = NotificationActionsViewState(
                    actions = getNotificationActionList()
                )
            )
        )
    val state: StateFlow<SettingsViewState> = _state

    init {
        viewModelScope.launch {
            val filters = filterHelper.get()
            _state.update {
                it.copy(filterViewState = it.filterViewState.copy(filters = filters))
            }
        }
    }

    fun syncRequested() {
        _state.update {
            it.copy(syncViewState = it.syncViewState.copy(loading = true))
        }
        viewModelScope.launch(dispatchers.io) {
            when (val syncResult = syncRepository.refresh()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            syncViewState = it.syncViewState.copy(
                                loading = false,
                                lastSyncTime = prefManager.getLastSyncTime()
                            )
                        )
                    }
                }
                is Result.Failure -> {
                    _state.update {
                        it.copy(
                            syncViewState = it.syncViewState.copy(loading = false),
                            error = syncResult.error
                        )
                    }
                }
            }
        }
    }

    fun filter(filter: Filter, filterItem: FilterItem) {
        val newFilter = filterHelper.onFilterItemSelected(filter, filterItem)
        val filters = _state.value.filterViewState.filters.toMutableList()
        filters.removeAll { it.getType() == filter.getType() }
        filters.add(newFilter)
        filters.sortBy { it.getType().index }
        _state.update {
            it.copy(filterViewState = it.filterViewState.copy(filters = filters))
        }
    }

    fun saveFilters() {
        val filters = _state.value.filterViewState.filters
        filters.forEach { filter ->
            val selectedIds = filter.getItems().filter { it.getIsSelected() }.map { it.getId() }
            when (filter.getType()) {
                FilterType.PERSON -> {
                    prefManager.setSavedPersonFilterIds(selectedIds)

                }
                FilterType.HOUSE -> {
                    prefManager.setSavedHouseFilterIds(selectedIds)
                }
            }
        }
    }

    fun onNotificationActionClicked(clicked: NotificationActionWrapper) {
        val actions = _state.value.notificationActionsViewState.actions
        // Attempting to select another when already 3 selected
        if (!clicked.selected && actions.filter { it.selected }.size == 3) {
            return
        }
        val newActions = actions.map {
            if (it.action == clicked.action) {
                it.copy(selected = !it.selected)
            } else {
                it
            }
        }
        _state.update {
            it.copy(
                notificationActionsViewState =
                it.notificationActionsViewState.copy(actions = newActions)
            )
        }
    }

    fun saveNotificationActions() {
        val actions = _state.value.notificationActionsViewState.actions
        prefManager.setEnabledNotificationActions(actions.filter { it.selected }.map { it.action })
    }

    fun onErrorShown() {
        _state.update {
            it.copy(error = null)
        }
    }

    private fun getNotificationActionList(): List<NotificationActionWrapper> {
        val savedActions = prefManager.getEnabledNotificationActions()
        return listOf(
            "SNOOZE_HOUR" to LocalizedString.NOTIFICATION_ACTION_SNOOZE_HOUR,
            "SNOOZE_DAY" to LocalizedString.NOTIFICATION_ACTION_SNOOZE_DAY,
            "COMPLETE" to LocalizedString.NOTIFICATION_ACTION_COMPLETE,
            "WONT_DO" to LocalizedString.NOTIFICATION_ACTION_WONT_DO
        ).map {
            NotificationActionWrapper(
                it.first,
                name = TextValue.ForKey(it.second),
                selected = savedActions.contains(it.first)
            )
        }
    }
}