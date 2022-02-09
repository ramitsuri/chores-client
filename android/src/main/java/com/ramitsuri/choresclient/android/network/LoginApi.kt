package com.ramitsuri.choresclient.android.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import javax.inject.Inject

class LoginApi @Inject constructor(
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