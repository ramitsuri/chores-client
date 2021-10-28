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
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaskAssignmentsRepository @Inject constructor(
    private val api: TaskAssignmentsApi,
    private val dataSource: TaskAssignmentDataSource,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun getTaskAssignments(
        getLocal: Boolean = false
    ): Result<List<TaskAssignment>> {
        if (getLocal) {
            return Result.Success(dataSource.getTaskAssignments())
        }
        return withContext(dispatcherProvider.io) {
            val result = try {
                api.getTaskAssignments()
            } catch (e: Exception) {
                null
            }

            if (result == null) {
                Result.Failure(ViewError.NETWORK)
            } else {
                val taskAssignments: List<TaskAssignment> = result.receive()
                dataSource.saveTaskAssignments(taskAssignments)
                Result.Success(dataSource.getTaskAssignments())
            }
        }
    }

    suspend fun filter(filterMode: FilterMode = FilterMode.ALL): Result<List<TaskAssignment>> {
        return Result.Success(dataSource.getTaskAssignments(filterMode))
    }

    suspend fun getTaskAssignment(id: String): Result<TaskAssignment> {
        return withContext(dispatcherProvider.io) {
            val result = try {
                api.getTaskAssignment(id)
            } catch (e: Exception) {
                null
            }

            if (result == null) {
                Result.Failure(ViewError.NETWORK)
            } else {
                Result.Success(result.receive())
            }
        }
    }

    suspend fun updateTaskAssignment(
        id: String,
        progressStatus: ProgressStatus
    ): Result<Boolean> {
        return withContext(dispatcherProvider.io) {
            val result = try {
                api.saveTaskAssignment(id, progressStatus)
            } catch (e: Exception) {
                null
            }

            if (result?.status == HttpStatusCode.OK) {
                Result.Success(true)
            } else {
                Result.Failure(ViewError.NETWORK)
            }
        }
    }
}