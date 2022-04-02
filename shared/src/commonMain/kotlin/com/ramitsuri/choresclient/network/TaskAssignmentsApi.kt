package com.ramitsuri.choresclient.network

import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.TaskAssignment
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

class TaskAssignmentsApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getTaskAssignments(): HttpResponse {
        return client.get("$baseUrl/task-assignments/filter?progress=1")
    }

    suspend fun updateTaskAssignments(taskAssignments: List<TaskAssignment>): HttpResponse {
        return client.put("$baseUrl/task-assignments") {
            body = taskAssignments.map { TaskAssignmentUpdate(it) }
        }
    }
}

@Serializable
data class TaskAssignmentUpdate(
    val id: String,
    @Serializable(with = ProgressStatusSerializer::class)
    val progressStatus: ProgressStatus,
    @Serializable(with = InstantSerializer::class)
    val progressStatusDate: Instant
) {
    constructor(taskAssignment: TaskAssignment) : this(
        taskAssignment.id,
        taskAssignment.progressStatus,
        taskAssignment.progressStatusDate
    )
}