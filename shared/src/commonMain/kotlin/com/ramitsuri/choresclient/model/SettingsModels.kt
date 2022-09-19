package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.ViewError
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

data class SettingsViewState(
    val syncViewState: SyncViewState,
    val filterViewState: FilterViewState = FilterViewState(),
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