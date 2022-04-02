package com.ramitsuri.choresclient.network

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

class LoginApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(id: String, key: String): HttpResponse {
        return client.post("$baseUrl/login") {
            body = LoginParam(id, key)
        }
    }
}

@Serializable
internal data class LoginParam(val id: String, val key: String)