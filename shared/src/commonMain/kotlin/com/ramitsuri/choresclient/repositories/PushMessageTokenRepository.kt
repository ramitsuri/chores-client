package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.network.PushMessageTokenApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.getToken
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext

class PushMessageTokenRepository(
    private val prefManager: PrefManager,
    private val api: PushMessageTokenApi,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: LogHelper
) {
    suspend fun submitToken(): Outcome {
        try {
            if (!prefManager.isLoggedIn()) {
                logger.v(TAG, "Not logged in")
                return Outcome.NOT_LOGGED_IN
            }

            val token = getToken(dispatcherProvider.io, logger, TAG)
            if (token == null) {
                logger.v(TAG, "Token not available")
                return Outcome.NO_TOKEN
            }

            val deviceId = prefManager.getDeviceId()
            if (deviceId == null) {
                logger.v(TAG, "No device id")
                return Outcome.NO_DEVICE_ID
            }

            val success = upload(token = token, deviceId = deviceId)
            if (!success) {
                logger.v(TAG, "Failed to upload token")
                return Outcome.UPLOAD_FAILED
            }
        } catch (e: Exception) {
            logger.v(TAG, "Failed to upload token: $e")
            return Outcome.EXCEPTION
        }
        return Outcome.SUCCESS


    }

    private suspend fun upload(token: String, deviceId: String): Boolean {
        return withContext(dispatcherProvider.io) {
            val result = try {
                api.submitToken(deviceId = deviceId, token = token)
            } catch (e: Exception) {
                null
            }
            when {
                result == null -> {
                    logger.v(TAG, "Failed to upload token: Network issue")
                    false
                }
                result.status == HttpStatusCode.OK -> {
                    true
                }
                else -> {
                    logger.v(TAG, "Failed to upload token: Unknown error")
                    false
                }
            }
        }
    }

    enum class Outcome {
        NOT_LOGGED_IN,
        NO_TOKEN,
        NO_DEVICE_ID,
        UPLOAD_FAILED,
        EXCEPTION,
        SUCCESS
    }

    companion object {
        private const val TAG = "TokenHelper"
    }
}