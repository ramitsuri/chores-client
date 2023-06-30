package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskDto
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.data.entities.TaskDao
import com.ramitsuri.choresclient.network.TasksApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SystemTasksRepository(
    private val api: TasksApi,
    private val taskDao: TaskDao,
    private val dispatcherProvider: DispatcherProvider
) : TasksRepository, KoinComponent {

    private val logger: LogHelper by inject()

    override suspend fun saveTask(taskId: String?, task: TaskDto): Result<Unit> {
        return if (taskId == null) {
            addTask(task)
        } else {
            editTask(taskId, task)
        }
    }

    private suspend fun editTask(taskId: String, task: TaskDto): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            val existingTask = taskDao.get(taskId)
            if (existingTask == null) {
                logger.v(TAG, "Existing task null, cannot edit")
                return@withContext Result.Failure(ViewError.EDIT_TASK_ERROR)
            }
            val newTask = TaskDto(
                name = existingTask.name.newOrNullIfSame(task.name),
                description = existingTask.description.newOrNullIfSame(task.description),
                dueDateTime = existingTask.dueDateTime.newOrNullIfSame(task.dueDateTime),
                repeatValue = existingTask.repeatValue.toInt().newOrNullIfSame(task.repeatValue),
                repeatUnit = existingTask.repeatUnit.newOrNullIfSame(task.repeatUnit),
                houseId = existingTask.houseId.newOrNullIfSame(task.houseId),
                memberId = existingTask.memberId.newOrNullIfSame(task.memberId),
                rotateMember = existingTask.rotateMember.newOrNullIfSame(task.rotateMember),
                status = existingTask.status.newOrNullIfSame(task.status),
            )

            logger.v(TAG, "Edit task requested: $newTask")
            val result = try {
                api.editTask(taskId, newTask)
            } catch (e: Exception) {
                logger.v(TAG, "Error while editing task: ${e.message}")
                null
            }

            return@withContext when (result?.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    Result.Success(Unit)
                }
                else -> {
                    Result.Failure(ViewError.EDIT_TASK_ERROR)
                }
            }
        }
    }

    private suspend fun addTask(task: TaskDto): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            logger.v(TAG, "Add task requested: $task")
            val result = try {
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

    private fun <T> T.newOrNullIfSame(new: T?): T? {
        return if (this == new) {
            null
        } else {
            new
        }
    }

    companion object {
        private const val TAG = "SystemTasksRepository"
    }
}

interface TasksRepository {
    suspend fun saveTask(taskId: String?, task: TaskDto): Result<Unit>
}