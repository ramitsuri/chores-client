package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.notification.NotificationActionInfo
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

data class SettingsViewState(
    val syncViewState: SyncViewState,
    val filterViewState: FilterViewState = FilterViewState(),
    val notificationActionsViewState: NotificationActionsViewState = NotificationActionsViewState(),
    val timeZone: TimeZone,
    val error: ViewError? = null
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