package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.error.Error
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

data class SettingsViewState(
    val syncViewState: SyncViewState,
    val notificationActionsViewState: NotificationActionsViewState = NotificationActionsViewState(),
    val deviceId: String?,
    val remoteLoggingEnabled: Boolean,
    val remindPastDueEnabled: Boolean,
    val allowTaskAssignmentUndo: Boolean,
    val swipeForTaskAssignmentCompletion: Boolean,
    val timeZone: TimeZone,
    val error: Error? = null
)

data class SyncViewState(
    val loading: Boolean = false,
    val lastSyncTime: Instant
)

data class NotificationActionsViewState(
    val actions: List<NotificationActionWrapper> = listOf()
)

data class NotificationActionWrapper(
    val action: String,
    val name: TextValue,
    val selected: Boolean
)
