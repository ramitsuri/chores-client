package com.ramitsuri.choresclient.android.repositories

import com.ramitsuri.choresclient.android.data.TaskAssignmentDataSource
import com.ramitsuri.choresclient.android.model.ProgressStatus
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
    private val dataSource: TaskAssignmentDataSource,
    private val dispatcherProvider: DispatcherProvider
) : TaskAssignmentsRepository {
    override suspend fun getTaskAssignments(
        getLocal: Boolean
    ): Result<List<TaskAssignment>> {
        if (getLocal) {
            val localAssignments = dataSource.getTaskAssignments()
            if (localAssignments.isNotEmpty()) {
                Timber.i("Returning local")
                return Result.Success(localAssignments)
            }
        }
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
                    dataSource.saveTaskAssignments(taskAssignments)
                    Result.Success(dataSource.getTaskAssignments())
                }
                else -> {
                    Result.Failure(ViewError.NETWORK)
                }
            }
        }
    }

    /**
     * Will always return locally saved assignments since the passed due date time
     */
    override suspend fun getSince(dueDateTime: Instant): List<TaskAssignment> {
        return dataSource.getSince(dueDateTime)
    }

    override suspend fun filter(filterMode: FilterMode): Result<List<TaskAssignment>> {
        return Result.Success(dataSource.getTaskAssignments(filterMode))
    }

    override suspend fun updateTaskAssignment(
        id: String,
        progressStatus: ProgressStatus
    ): Result<Boolean> {
        return withContext(dispatcherProvider.io) {
            val result = try {
                api.updateTaskAssignment(id, progressStatus)
            } catch (e: Exception) {
                null
            }

            when (result?.status) {
                HttpStatusCode.OK -> {
                    val taskAssignment: TaskAssignment = result.receive()
                    dataSource.saveTaskAssignment(taskAssignment)
                    Result.Success(true)
                }
                else -> {
                    Result.Failure(ViewError.NETWORK)
                }
            }
        }
    }
}

interface TaskAssignmentsRepository {
    suspend fun getTaskAssignments(
        getLocal: Boolean = false
    ): Result<List<TaskAssignment>>

    suspend fun getSince(dueDateTime: Instant): List<TaskAssignment>

    suspend fun filter(filterMode: FilterMode): Result<List<TaskAssignment>>

    suspend fun updateTaskAssignment(
        id: String,
        progressStatus: ProgressStatus
    ): Result<Boolean>
}