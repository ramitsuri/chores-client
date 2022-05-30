package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.FilterMode
import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.network.TaskAssignmentsApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
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
        logger.v(TAG, "Uploaded: ${uploadedIds.joinToString()}")

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
    override suspend fun getLocal(sinceDueDateTime: Instant): List<TaskAssignment> {
        return localDataSource.getSince(sinceDueDateTime)
    }

    override suspend fun getLocal(filterMode: FilterMode): Result<List<TaskAssignment>> {
        return Result.Success(localDataSource.getTaskAssignments(filterMode))
    }

    override suspend fun getLocal(id: String): TaskAssignment? {
        return localDataSource.getTaskAssignment(id)
    }

    override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {
        logger.v(TAG, "Mark $taskAssignmentId done requested")
        localDataSource.markDone(taskAssignmentId, doneTime)
        logger.v(TAG, "Mark $taskAssignmentId done completed")
    }

    private suspend fun uploadLocal(): List<String> {
        return withContext(dispatcherProvider.io) {
            val readyForUpload = localDataSource.getReadyForUpload()
            val uploadResult = try {
                api.updateTaskAssignments(readyForUpload)
            } catch (e: Exception) {
                null
            }

            return@withContext when (uploadResult?.status) {
                HttpStatusCode.OK -> {
                    val uploadedTaskAssignmentIds: List<String> = uploadResult.body()
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

    suspend fun getLocal(sinceDueDateTime: Instant): List<TaskAssignment>

    suspend fun getLocal(filterMode: FilterMode): Result<List<TaskAssignment>>

    suspend fun getLocal(id: String): TaskAssignment?

    suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant)
}