package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.TaskAssignmentUpdate
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.network.InstantSerializer
import com.ramitsuri.choresclient.network.ProgressStatusSerializer
import com.ramitsuri.choresclient.network.apiRequest
import com.ramitsuri.choresclient.network.model.TaskAssignmentDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.time.Instant
import kotlinx.serialization.Serializable

class TaskAssignmentsApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getTaskAssignments(
        progressStatus: List<ProgressStatus> = listOf(ProgressStatus.TODO),
        dueDateWithinDays: Int? = null,
    ): Result<List<TaskAssignmentDto>> {
        return apiRequest(ioDispatcher) {
            client.get("$baseUrl/task-assignments/filter") {
                url {
                    parameters.append(
                        name = "progress",
                        value = progressStatus.joinToString(separator = ",") { it.key.toString() },
                    )
                    if (dueDateWithinDays != null) {
                        parameters.append(
                            name = "duedatewithindays",
                            value = dueDateWithinDays.toString(),
                        )
                    }
                }
            }
        }
    }

    suspend fun updateTaskAssignments(taskAssignmentUpdates: List<TaskAssignmentUpdate>): Result<List<String>> {
        return apiRequest(ioDispatcher) {
            client.put("$baseUrl/task-assignments") {
                contentType(ContentType.Application.Json)
                setBody(taskAssignmentUpdates.map { TaskAssignmentUpdateDto(it) })
            }
        }
    }
}

@Serializable
private data class TaskAssignmentUpdateDto(
    val id: String,
    @Serializable(with = ProgressStatusSerializer::class)
    val progressStatus: ProgressStatus,
    @Serializable(with = InstantSerializer::class)
    val progressStatusDate: Instant,
    val shouldTransfer: Boolean = false
) {
    constructor(update: TaskAssignmentUpdate) : this(
        id = update.id,
        progressStatus = update.progressStatus,
        progressStatusDate = update.progressStatusDate,
        shouldTransfer = update.shouldTransfer
    )
}
