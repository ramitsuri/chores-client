package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.error.PushTokenError
import com.ramitsuri.choresclient.network.api.PushMessageTokenApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.getToken

class PushMessageTokenRepository(
    private val prefManager: PrefManager,
    private val api: PushMessageTokenApi,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: LogHelper
) {
    suspend fun submitToken(): Result<Unit> {
        if (!prefManager.isLoggedIn()) {
            logger.v(TAG, "Not logged in")
            return Result.Failure(PushTokenError.NotLoggedIn)
        }

        val token = getToken(dispatcherProvider.io, logger, TAG)
        if (token == null) {
            logger.v(TAG, "Token not available")
            return Result.Failure(PushTokenError.NoToken)
        }

        val deviceId = prefManager.getDeviceId()
        if (deviceId == null) {
            logger.v(TAG, "No device id")
            return Result.Failure(PushTokenError.NoDeviceId)
        }

        val result = api.submitToken(deviceId = deviceId, token = token)
        if (result !is Result.Success) {
            logger.v(TAG, "Failed to upload token")
        }
        return result
    }

    companion object {
        private const val TAG = "TokenHelper"
    }
}