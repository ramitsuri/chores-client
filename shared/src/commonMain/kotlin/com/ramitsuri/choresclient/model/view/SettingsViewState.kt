package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.error.Error
import com.ramitsuri.choresclient.model.filter.Filter
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

data class SettingsViewState(
    val syncViewState: SyncViewState,
    val filterViewState: FilterViewState = FilterViewState(),
    val notificationActionsViewState: NotificationActionsViewState = NotificationActionsViewState(),
    val deviceId: String?,
    val remoteLoggingEnabled: Boolean,
    val remindPastDueEnabled: Boolean,
    val timeZone: TimeZone,
    val error: Error? = null
)

data class SyncViewState(
    val loading: Boolean = false,
    val lastSyncTime: Instant
)

data class FilterViewState(
    val filters: List<Filter> = listOf()
)

data class NotificationActionsViewState(
    val actions: List<NotificationActionWrapper> = listOf()
)

data class NotificationActionWrapper(
    val action: String,
    val name: TextValue,
    val selected: Boolean
)