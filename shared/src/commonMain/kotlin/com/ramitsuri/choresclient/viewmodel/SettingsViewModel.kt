package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem
import com.ramitsuri.choresclient.model.filter.FilterType
import com.ramitsuri.choresclient.model.view.NotificationActionWrapper
import com.ramitsuri.choresclient.model.view.NotificationActionsViewState
import com.ramitsuri.choresclient.model.view.SettingsViewState
import com.ramitsuri.choresclient.model.view.SyncViewState
import com.ramitsuri.choresclient.model.view.TextValue
import com.ramitsuri.choresclient.resources.LocalizedString
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.FilterHelper
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val contentDownloadRequestHandler: ContentDownloadRequestHandler,
    private val filterHelper: FilterHelper,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider,
    private val logger: LogHelper
) : ViewModel(), KoinComponent {

    private val _state =
        MutableStateFlow(
            SettingsViewState(
                syncViewState = SyncViewState(lastSyncTime = prefManager.getLastSyncTime()),
                timeZone = TimeZone.currentSystemDefault(),
                notificationActionsViewState = NotificationActionsViewState(
                    actions = getNotificationActionList()
                ),
                deviceId = prefManager.getDeviceId(),
                remoteLoggingEnabled = prefManager.getEnableRemoteLogging(),
                remindPastDueEnabled = prefManager.remindPastDue(),
            )
        )
    val state: StateFlow<SettingsViewState> = _state

    init {
        resetFilters()
    }

    fun syncRequested() {
        _state.update {
            it.copy(syncViewState = it.syncViewState.copy(loading = true))
        }
        viewModelScope.launch(dispatchers.io) {
            // ForceRemind if download requested manually because the user is probably wanting a
            // full refresh
            contentDownloadRequestHandler.requestImmediateDownload(
                forceRemindPastDue = true,
                forceRemindFuture = true,
            ).collect { running ->
                _state.update {
                    it.copy(
                        syncViewState = it.syncViewState.copy(
                            loading = running,
                            lastSyncTime = prefManager.getLastSyncTime()
                        )
                    )
                }
            }
        }
    }

    fun filter(filter: Filter, filterItem: FilterItem) {
        val newFilter = filterHelper.onFilterItemClicked(filter, filterItem)
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

    fun resetFilters() {
        viewModelScope.launch {
            val filters = filterHelper.getBaseFilters()
            _state.update {
                it.copy(filterViewState = it.filterViewState.copy(filters = filters))
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

    fun resetNotificationActions() {
        _state.update {
            it.copy(
                notificationActionsViewState = NotificationActionsViewState(
                    actions = getNotificationActionList()
                )
            )
        }
        val actions = _state.value.notificationActionsViewState.actions
        prefManager.setEnabledNotificationActions(actions.filter { it.selected }.map { it.action })
    }

    fun toggleLogging() {
        val enabled = !prefManager.getEnableRemoteLogging()
        prefManager.setEnableRemoteLogging(enabled)
        logger.enableRemoteLogging(enabled)
        _state.update {
            it.copy(remoteLoggingEnabled = enabled)
        }
    }

    fun toggleRemindPastDue() {
        val enabled = !prefManager.remindPastDue()
        prefManager.setRemindPastDue(enabled)
        _state.update {
            it.copy(remindPastDueEnabled = enabled)
        }
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