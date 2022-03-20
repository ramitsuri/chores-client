package com.ramitsuri.choresclient.android.repositories

import com.ramitsuri.choresclient.android.data.TaskAssignmentDataSource
import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewError
import com.ramitsuri.choresclient.android.network.TaskAssignmentsApi
import com.ramitsuri.choresclient.android.ui.assigments.FilterMode
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import io.ktor.client.call.receive
import io.ktor.http.HttpStatusCode
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.withContext
import timber.log.Timber

class SystemTaskAssignmentsRepository @Inject constructor(
    private val api: TaskAssignmentsApi,
    private val localDataSource: TaskAssignmentDataSource,
    private val dispatcherProvider: DispatcherProvider
) : TaskAssignmentsRepository {

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
    override suspend fun getLocal(sinceDueDateTime: Instant): List<TaskAssignment> {
        return localDataSource.getSince(sinceDueDateTime)
    }

    override suspend fun getLocal(filterMode: FilterMode): Result<List<TaskAssignment>> {
        return Result.Success(localDataSource.getTaskAssignments(filterMode))
    }

    override suspend fun getLocal(id: String): TaskAssignment? {
        return localDataSource.getTaskAssignment(id)
    }

    override suspend fun updateTaskAssignment(
        taskAssignment: TaskAssignment,
        readyForUpload: Boolean
    ) {
        return withContext(dispatcherProvider.io) {
            localDataSource.update(taskAssignment, readyForUpload)
        }
    }

    private suspend fun uploadLocal(): List<String> {
        val readyForUpload = localDataSource.getReadyForUpload()
        val uploadResult = try {
            api.updateTaskAssignments(readyForUpload)
        } catch (e: Exception) {
            Timber.i("Caught exception $e")
            null
        }

        return when (uploadResult?.status) {
            HttpStatusCode.OK -> {
                val uploadedTaskAssignmentIds: List<String> = uploadResult.receive()
                uploadedTaskAssignmentIds
            }
            else -> {
                listOf()
            }
        }
    }

    private suspend fun fetchAndSave(): Boolean {
        return withContext(dispatcherProvider.io) {
            val result = try {
                api.getTaskAssignments()
            } catch (e: Exception) {
                Timber.i("Caught exception $e")
                null
            }

            when (result?.status) {
                HttpStatusCode.OK -> {
                    val taskAssignments: List<TaskAssignment> = result.receive()
                    localDataSource.saveTaskAssignments(taskAssignments)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }
}

interface TaskAssignmentsRepository {
    suspend fun refresh(): Result<List<TaskAssignment>>

    suspend fun getLocal(sinceDueDateTime: Instant): List<TaskAssignment>

    suspend fun getLocal(filterMode: FilterMode): Result<List<TaskAssignment>>

    suspend fun getLocal(id: String): TaskAssignment?

    suspend fun updateTaskAssignment(
        taskAssignment: TaskAssignment,
        readyForUpload: Boolean
    )
}