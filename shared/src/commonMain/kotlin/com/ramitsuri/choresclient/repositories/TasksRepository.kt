package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.db.dao.TaskDao
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.network.api.TasksApi
import com.ramitsuri.choresclient.network.model.AddTaskRequest
import com.ramitsuri.choresclient.network.model.EditTaskRequest
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultTasksRepository(
    private val api: TasksApi,
    private val taskDao: TaskDao,
) : TasksRepository, KoinComponent {

    private val logger: LogHelper by inject()

    override suspend fun getTask(id: String): Task? {
        val taskEntity = taskDao.get(id) ?: return null
        return Task(taskEntity)
    }

    override suspend fun editTask(
        taskId: String,
        name: String?,
        description: String?,
        dueDateTime: LocalDateTime?,
        repeatValue: Long?,
        repeatUnit: RepeatUnit?,
        repeatEndDateTime: LocalDateTime?,
        rotateMember: Boolean?,
        status: ActiveStatus?,
    ): Result<Unit> {
        val request = EditTaskRequest(
            name = name,
            description = description,
            dueDateTime = dueDateTime,
            repeatValue = repeatValue?.toInt(),
            repeatUnit = repeatUnit,
            repeatEndDateTime = repeatEndDateTime,
            rotateMember = rotateMember,
            status = status,
            houseId = null,
            memberId = null,
        )

        logger.v(TAG, "Edit task requested: $request")
        return api.editTask(taskId, request)
    }

    override suspend fun addTask(
        name: String,
        description: String,
        dueDateTime: LocalDateTime,
        repeatValue: Long,
        repeatUnit: RepeatUnit,
        repeatEndDateTime: LocalDateTime?,
        houseId: String,
        memberId: String,
        rotateMember: Boolean,
    ): Result<Unit> {
        val request = AddTaskRequest(
            name = name,
            description = description,
            dueDateTime = dueDateTime,
            repeatValue = repeatValue.toInt(),
            repeatUnit = repeatUnit,
            repeatEndDateTime = repeatEndDateTime,
            houseId = houseId,
            memberId = memberId,
            rotateMember = rotateMember,
            status = ActiveStatus.ACTIVE
        )

        logger.v(TAG, "Add task requested: $request")
        return api.addTask(request)
    }

    companion object {
        private const val TAG = "TasksRepository"
    }
}

interface TasksRepository {

    suspend fun getTask(id: String): Task?

    suspend fun addTask(
        name: String,
        description: String,
        dueDateTime: LocalDateTime,
        repeatValue: Long,
        repeatUnit: RepeatUnit,
        repeatEndDateTime: LocalDateTime?,
        houseId: String,
        memberId: String,
        rotateMember: Boolean,
    ): Result<Unit>

    suspend fun editTask(
        taskId: String,
        name: String?,
        description: String?,
        dueDateTime: LocalDateTime?,
        repeatValue: Long?,
        repeatUnit: RepeatUnit?,
        repeatEndDateTime: LocalDateTime?,
        rotateMember: Boolean?,
        status: ActiveStatus?
    ): Result<Unit>
}