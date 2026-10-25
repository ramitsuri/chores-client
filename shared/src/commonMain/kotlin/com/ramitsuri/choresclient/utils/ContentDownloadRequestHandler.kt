package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.flow.Flow

data class SyncStatus(
    val isUploading: Boolean = false,
    val isScheduled: Boolean = false,
)

interface ContentDownloadRequestHandler {
    fun requestImmediateDownload(
        forceDownload: Boolean = false,
        forceRemindPastDue: Boolean = false,
        forceRemindFuture: Boolean = false
    ): Flow<Boolean>

    fun requestDelayedDownload(
        forceRemindPastDue: Boolean = false,
        forceRemindFuture: Boolean = false
    )

    fun getSyncStatusFlow(): Flow<SyncStatus>
}
