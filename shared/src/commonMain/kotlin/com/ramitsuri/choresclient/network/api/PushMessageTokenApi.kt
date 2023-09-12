package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.network.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.Serializable

class PushMessageTokenApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun submitToken(deviceId: String, token: String): Result<Unit> {
        return apiRequest(ioDispatcher) {
            client.put("$baseUrl/push-token") {
                setBody(PushMessageToken(deviceId = deviceId, token = token))
            }
        }
    }
}

@Serializable
private data class PushMessageToken(
    val deviceId: String,
    val token: String
)