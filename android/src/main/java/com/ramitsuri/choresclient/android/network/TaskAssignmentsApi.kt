package com.ramitsuri.choresclient.android.network

import com.ramitsuri.choresclient.android.model.InstantSerializer
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.ProgressStatusSerializer
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.TaskAssignmentDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import java.time.Instant
import javax.inject.Inject
import kotlinx.serialization.Serializable

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