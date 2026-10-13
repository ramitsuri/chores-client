package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.db.dao.HouseDao
import com.ramitsuri.choresclient.data.db.dao.MemberDao
import com.ramitsuri.choresclient.data.db.dao.MemberHouseAssociationDao
import com.ramitsuri.choresclient.data.db.dao.TaskDao
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.TaskDetails
import com.ramitsuri.choresclient.network.api.TasksApi
import com.ramitsuri.choresclient.network.model.AddTaskRequest
import com.ramitsuri.choresclient.network.model.EditTaskRequest
import com.ramitsuri.choresclient.network.model.toTaskEntity
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultTasksRepository(
    private val api: TasksApi,
    private val taskDao: TaskDao,
    private val memberDao: MemberDao,
    private val houseDao: HouseDao,
    private val memberHouseAssociationDao: MemberHouseAssociationDao,
) : TasksRepository, KoinComponent {

    private val logger: LogHelper by inject()

    override suspend fun fetchTasks(): Result<Unit> {
        return when (val result = api.getTasks()) {
            is Result.Failure -> Result.Failure(result.error)
            is Result.Success -> {
                val taskEntities = result.data.map { it.toTaskEntity() }
                taskDao.insert(taskEntities)
                logger.v(TAG, "Fetched tasks: ${result.data.joinToString { it.id }}")
                Result.Success(Unit)
            }
        }
    }

    override fun getTasksFlow(): Flow<List<TaskDetails>> {
        return taskDao.getAllFlow().map { taskEntities ->
            val members = memberDao.get().associateBy { it.id }
            val houses = houseDao.get().associateBy { it.id }
            taskEntities.map { entity ->
                val task = Task(entity)
                val memberName = members[task.memberId]?.name ?: ""
                val houseName = houses[task.houseId]?.name ?: ""
                val isSharedList = memberHouseAssociationDao.getForHouse(task.houseId).size > 1
                TaskDetails(
                    task = task,
                    memberName = memberName,
                    houseName = houseName,
                    isSharedList = isSharedList
                )
            }
        }
    }

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

    suspend fun fetchTasks(): Result<Unit>

    fun getTasksFlow(): Flow<List<TaskDetails>>

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
