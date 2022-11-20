package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskDto
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.network.TasksApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SystemTasksRepository(
    private val api: TasksApi,
    private val dispatcherProvider: DispatcherProvider
) : TasksRepository, KoinComponent {

    private val logger: LogHelper by inject()

    override suspend fun addTask(task: TaskDto): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            logger.v(TAG, "Add task requested: $task")
            val result = try {
                logger.v(TAG, "Task added")
                api.addTask(task)
            } catch (e: Exception) {
                logger.v(TAG, "Error while adding task: ${e.message}")
                null
            }

            return@withContext when (result?.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    Result.Success(Unit)
                }
                else -> {
                    Result.Failure(ViewError.ADD_TASK_ERROR)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SystemTasksRepository"
    }
}

interface TasksRepository {
    suspend fun addTask(task: TaskDto): Result<Unit>
}