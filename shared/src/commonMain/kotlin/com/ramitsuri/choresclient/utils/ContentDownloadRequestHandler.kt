package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.flow.Flow

interface ContentDownloadRequestHandler {
    fun requestImmediateDownload(): Flow<Boolean>
}