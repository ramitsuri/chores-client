package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.flow.Flow

interface ContentDownloadRequestHandler {
    fun requestImmediateDownload(forceRemindPastDue: Boolean = false): Flow<Boolean>

    fun requestDelayedDownload(forceRemindPastDue: Boolean = false)
}