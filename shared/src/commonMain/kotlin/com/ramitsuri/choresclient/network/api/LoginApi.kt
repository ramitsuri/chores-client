package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.network.apiRequest
import com.ramitsuri.choresclient.network.model.TokenDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.Serializable

class LoginApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun login(id: String, key: String): Result<TokenDto> {
        return apiRequest(ioDispatcher) {
            client.post("$baseUrl/login") {
                setBody(LoginParam(id, key))
            }
        }
    }
}

@Serializable
private data class LoginParam(val id: String, val key: String)