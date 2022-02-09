package com.ramitsuri.choresclient.android.network

import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.TaskAssignmentDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import javax.inject.Inject

class TaskAssignmentsApi @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getTaskAssignments(): HttpResponse {
        return client.get("$baseUrl/task-assignments/filter?progress=1")
    }

    suspend fun updateTaskAssignment(
        id: String,
        progressStatus: ProgressStatus
    ): HttpResponse {
        return client.put("$baseUrl/task-assignments/$id") {
            body = TaskAssignmentDto(progressStatus.key)
        }
    }
}