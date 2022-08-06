package com.ramitsuri.choresclient.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

class SyncApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun sync(): HttpResponse {
        return client.get("$baseUrl/sync")
    }
}