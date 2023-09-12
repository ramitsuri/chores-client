package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.network.apiRequest
import com.ramitsuri.choresclient.network.model.SyncResultDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher

class SyncApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun sync(): Result<SyncResultDto> {
        return apiRequest(ioDispatcher) {
            client.get("$baseUrl/sync")
        }
    }
}