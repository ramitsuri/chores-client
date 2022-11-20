package com.ramitsuri.choresclient.network

import com.ramitsuri.choresclient.data.TaskDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class TasksApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun addTask(task: TaskDto): HttpResponse {
        return client.post("$baseUrl/tasks") {
            setBody(task)
        }
    }
}