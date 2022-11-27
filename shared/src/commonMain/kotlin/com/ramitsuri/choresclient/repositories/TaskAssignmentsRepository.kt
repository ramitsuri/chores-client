package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.network.TaskAssignmentsApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SystemTaskAssignmentsRepository(
    private val api: TaskAssignmentsApi,
    private val localDataSource: TaskAssignmentDataSource,
    private val dispatcherProvider: DispatcherProvider
) : TaskAssignmentsRepository, KoinComponent {

    private val logger: LogHelper by inject()
    override suspend fun refresh(): Result<List<TaskAssignment>> {
        // Upload completed local assignments
        val uploadedIds = uploadLocal()

        // Delete Ids that have been confirmed to be deleted by server
        localDataSource.delete(uploadedIds)

        // Fetch from server and save locally. Return locally saved ones
        val fetchedAndSaved = fetchAndSave()
        return if (fetchedAndSaved) {
            Result.Success(localDataSource.getTaskAssignments())
        } else {
            Result.Failure(ViewError.NETWORK)
        }
    }

    /**
     * Will always return locally saved assignments since the passed due date time
     */
    override suspend fun getLocal(sinceDueDateTime: LocalDateTime): List<TaskAssignment> {
        return localDataSource.getSince(sinceDueDateTime)
    }

    override suspend fun getLocal(filters: List<Filter>): Result<List<TaskAssignment>> {
        return Result.Success(localDataSource.getTaskAssignments(filters))
    }

    override suspend fun getLocal(id: String): TaskAssignment? {
        return localDataSource.getTaskAssignment(id)
    }

    override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {
        localDataSource.markDone(taskAssignmentId, doneTime)
        logger.v(
            TAG,
            "Mark $taskAssignmentId done completed. New status: ${
                localDataSource.getTaskAssignmentStatus(taskAssignmentId)
            }"
        )
    }

    override suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant) {
        localDataSource.markWontDo(taskAssignmentId, wontDoTime)
        logger.v(
            TAG,
            "Mark $taskAssignmentId won't do completed. New status: ${
                localDataSource.getTaskAssignmentStatus(taskAssignmentId)
            }"
        )
    }

    private suspend fun uploadLocal(): List<String> {
        return withContext(dispatcherProvider.io) {
            val readyForUpload = localDataSource.getReadyForUpload()
            logger.v(TAG, "Ready for upload: ${readyForUpload.joinToString()}")
            val uploadResult = try {
                api.updateTaskAssignments(readyForUpload)
            } catch (e: Exception) {
                null
            }

            return@withContext when (uploadResult?.status) {
                HttpStatusCode.OK -> {
                    val uploadedTaskAssignmentIds: List<String> = uploadResult.body()
                    logger.v(TAG, "Uploaded: ${uploadedTaskAssignmentIds.joinToString()}")
                    uploadedTaskAssignmentIds
                }
                else -> {
                    listOf()
                }
            }
        }
    }

    private suspend fun fetchAndSave(): Boolean {
        return withContext(dispatcherProvider.io) {
            val result = try {
                api.getTaskAssignments()
            } catch (e: Exception) {
                null
            }

            when (result?.status) {
                HttpStatusCode.OK -> {
                    val taskAssignments: List<TaskAssignment> = result.body()
                    localDataSource.saveTaskAssignments(taskAssignments)
                    logger.v(TAG, "Fetched: ${taskAssignments.joinToString { it.id }}")
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    companion object {
        private const val TAG = "SystemTaskAssignmentsRepository"
    }
}

interface TaskAssignmentsRepository {
    suspend fun refresh(): Result<List<TaskAssignment>>

    suspend fun getLocal(sinceDueDateTime: LocalDateTime): List<TaskAssignment>

    suspend fun getLocal(filters: List<Filter>): Result<List<TaskAssignment>>

    suspend fun getLocal(id: String): TaskAssignment?

    suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant)

    suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant)
}