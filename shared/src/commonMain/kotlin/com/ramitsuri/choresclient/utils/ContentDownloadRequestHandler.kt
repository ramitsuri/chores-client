package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.flow.Flow

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
}