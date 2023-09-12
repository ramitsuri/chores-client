package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.network.InstantSerializer
import com.ramitsuri.choresclient.network.ProgressStatusSerializer
import com.ramitsuri.choresclient.network.apiRequest
import com.ramitsuri.choresclient.network.model.TaskAssignmentDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

class TaskAssignmentsApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getTaskAssignments(): Result<List<TaskAssignmentDto>> {
        return apiRequest(ioDispatcher) {
            client.get("$baseUrl/task-assignments/filter?progress=1")
        }
    }

    suspend fun updateTaskAssignments(taskAssignments: List<TaskAssignment>): Result<List<String>> {
        return apiRequest(ioDispatcher) {
            client.put("$baseUrl/task-assignments") {
                setBody(taskAssignments.map { TaskAssignmentUpdate(it) })
            }
        }
    }
}

@Serializable
private data class TaskAssignmentUpdate(
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