package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.flow.Flow

interface TaskUploadRequestHandler {
    fun requestImmediateUpload(): Flow<Boolean>

    fun requestDelayedUpload()

    fun getUploadStatusFlow(): Flow<SyncStatus>
}
