package com.ramitsuri.choresclient.network

import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

class PushMessageTokenApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun submitToken(deviceId: String, token: String): HttpResponse {
        return client.put("$baseUrl/push-token") {
            setBody(PushMessageToken(deviceId = deviceId, token = token))
        }
    }
}

@Serializable
internal data class PushMessageToken(
    val deviceId: String,
    val token: String
)