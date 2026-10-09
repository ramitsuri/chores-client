package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.network.apiRequest
import com.ramitsuri.choresclient.network.model.AddTaskRequest
import com.ramitsuri.choresclient.network.model.EditTaskRequest
import com.ramitsuri.choresclient.network.model.TaskDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineDispatcher

class TasksApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getTasks(): Result<List<TaskDto>> {
        return apiRequest(ioDispatcher) {
            client.get("$baseUrl/tasks")
        }
    }
    suspend fun addTask(task: AddTaskRequest): Result<Unit> {
        return apiRequest(ioDispatcher) {
            client.post("$baseUrl/tasks") {
                setBody(task)
            }
        }
    }

    suspend fun editTask(taskId: String, task: EditTaskRequest): Result<Unit> {
        return apiRequest(ioDispatcher) {
            client.put("$baseUrl/tasks/$taskId") {
                setBody(task)
            }
        }
    }
}