package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.db.dao.HouseDao
import com.ramitsuri.choresclient.data.db.dao.MemberDao
import com.ramitsuri.choresclient.data.db.dao.MemberHouseAssociationDao
import com.ramitsuri.choresclient.data.db.dao.TaskDao
import com.ramitsuri.choresclient.data.db.dao.TaskOutboxDao
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.db.TaskOutboxEntity
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.error.EditTaskError
import com.ramitsuri.choresclient.model.view.TaskDetails
import com.ramitsuri.choresclient.network.api.TasksApi
import com.ramitsuri.choresclient.network.model.TaskBatchDto
import com.ramitsuri.choresclient.network.model.toTaskEntity
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.TaskUploadRequestHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import com.ramitsuri.choresclient.data.db.dao.TaskAssignmentDao
import kotlin.uuid.Uuid

class DefaultTasksRepository(
    private val api: TasksApi,
    private val taskDao: TaskDao,
    private val taskOutboxDao: TaskOutboxDao,
    private val taskAssignmentDao: TaskAssignmentDao,
    private val memberDao: MemberDao,
    private val houseDao: HouseDao,
    private val memberHouseAssociationDao: MemberHouseAssociationDao,
    private val taskUploadRequestHandler: TaskUploadRequestHandler,
) : TasksRepository, KoinComponent {

    private val logger: LogHelper by inject()

    override suspend fun uploadLocal(): List<String> {
        val outboxItems = taskOutboxDao.getOutboxItems()
        logger.v(TAG, "Outbox items for upload: ${outboxItems.joinToString { it.id }}")
        if (outboxItems.isEmpty()) {
            return listOf()
        }

        val batchRequests = outboxItems.map { item ->
            val clientRequestId = item.id
            val repeatUnit = if (item.repeatUnit == RepeatUnit.NONE) null else item.repeatUnit
            val repeatValue = if (item.repeatUnit == RepeatUnit.NONE) null else item.repeatValue?.toInt()

            if (item.type == "add") {
                TaskBatchDto.Add(
                    name = item.name ?: "",
                    description = item.description ?: "",
                    dueDateTime = item.dueDateTime ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
                    houseId = item.houseId ?: "",
                    memberId = item.memberId ?: "",
                    clientRequestId = clientRequestId,
                    repeatValue = repeatValue,
                    repeatUnit = repeatUnit,
                    repeatEndDateTime = item.repeatEndDateTime,
                    rotateMember = item.rotateMember,
                    status = item.status ?: ActiveStatus.ACTIVE,
                )
            } else {
                TaskBatchDto.Edit(
                    taskId = item.taskId ?: "",
                    clientRequestId = clientRequestId,
                    name = item.name,
                    description = item.description,
                    dueDateTime = item.dueDateTime,
                    repeatValue = repeatValue,
                    repeatUnit = repeatUnit,
                    repeatEndDateTime = item.repeatEndDateTime,
                    rotateMember = item.rotateMember,
                    status = item.status,
                )
            }
        }

        return when (val result = api.batchTasks(batchRequests)) {
            is Result.Failure -> {
                logger.v(TAG, "Upload tasks failed: ${result.error}")
                listOf()
            }

            is Result.Success -> {
                val batchResponse = result.data
                val succeededClientRequestIds = batchResponse.successfulRequestIds
                logger.v(TAG, "Succeeded clientRequestIds: ${succeededClientRequestIds.joinToString()}")
                logger.v(TAG, "Delete task assignment task IDs: ${batchResponse.deleteTaskAssignmentsTaskIds.joinToString()}")

                taskOutboxDao.delete(succeededClientRequestIds)
                if (batchResponse.deleteTaskAssignmentsTaskIds.isNotEmpty()) {
                    taskAssignmentDao.deleteForTasks(batchResponse.deleteTaskAssignmentsTaskIds)
                }

                succeededClientRequestIds
            }
        }
    }

    override suspend fun fetchTasks(): Result<Unit> {
        return when (val result = api.getTasks()) {
            is Result.Failure -> Result.Failure(result.error)
            is Result.Success -> {
                val serverTaskEntities = result.data.map { it.toTaskEntity() }
                taskDao.clearAndInsert(serverTaskEntities)
                logger.v(TAG, "Fetched tasks: ${result.data.joinToString { it.id }}")
                Result.Success(Unit)
            }
        }
    }

    override fun getTasksFlow(): Flow<List<TaskDetails>> {
        return combine(taskDao.getAllFlow(), taskOutboxDao.getOutboxFlow()) { taskEntities, outboxEntities ->
            val members = memberDao.get().associateBy { it.id }
            val houses = houseDao.get().associateBy { it.id }

            val tasksMap = taskEntities.associateBy { it.id }.toMutableMap()

            // Apply pending edits from outbox
            outboxEntities.filter { it.type == "edit" }.forEach { editOutbox ->
                val taskId = editOutbox.taskId ?: return@forEach
                val existing = tasksMap[taskId] ?: return@forEach
                tasksMap[taskId] = TaskEntity(
                    id = existing.id,
                    name = editOutbox.name ?: existing.name,
                    description = editOutbox.description ?: existing.description,
                    dueDateTime = editOutbox.dueDateTime ?: existing.dueDateTime,
                    repeatValue = editOutbox.repeatValue ?: existing.repeatValue,
                    repeatUnit = editOutbox.repeatUnit ?: existing.repeatUnit,
                    repeatEndDateTime = editOutbox.repeatEndDateTime ?: existing.repeatEndDateTime,
                    houseId = existing.houseId,
                    memberId = existing.memberId,
                    rotateMember = editOutbox.rotateMember ?: existing.rotateMember,
                    createdDate = existing.createdDate,
                    status = editOutbox.status ?: existing.status
                )
            }

            val syncedTaskDetails = tasksMap.values.map { entity ->
                val task = Task(entity)
                val memberName = members[task.memberId]?.name ?: ""
                val houseName = houses[task.houseId]?.name ?: ""
                val isSharedList = memberHouseAssociationDao.getForHouse(task.houseId).size > 1
                val isPendingEdit = outboxEntities.any { it.type == "edit" && it.taskId == entity.id }
                TaskDetails(
                    task = task,
                    memberName = memberName,
                    houseName = houseName,
                    isSharedList = isSharedList,
                    isSyncPending = isPendingEdit
                )
            }

            // Add pending additions from outbox
            val pendingAddDetails = outboxEntities.filter { it.type == "add" }.map { addOutbox ->
                val task = Task(
                    id = addOutbox.id,
                    name = addOutbox.name ?: "",
                    description = addOutbox.description ?: "",
                    dueDateTime = addOutbox.dueDateTime ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
                    repeatValue = addOutbox.repeatValue?.toInt() ?: 0,
                    repeatUnit = addOutbox.repeatUnit ?: RepeatUnit.NONE,
                    repeatEndDateTime = addOutbox.repeatEndDateTime,
                    houseId = addOutbox.houseId ?: "",
                    memberId = addOutbox.memberId ?: "",
                    rotateMember = addOutbox.rotateMember ?: false,
                    createdDate = addOutbox.createdDate,
                    status = addOutbox.status ?: ActiveStatus.ACTIVE
                )
                val memberName = members[task.memberId]?.name ?: ""
                val houseName = houses[task.houseId]?.name ?: ""
                val isSharedList = memberHouseAssociationDao.getForHouse(task.houseId).size > 1
                TaskDetails(
                    task = task,
                    memberName = memberName,
                    houseName = houseName,
                    isSharedList = isSharedList,
                    isSyncPending = true
                )
            }

            syncedTaskDetails + pendingAddDetails
        }
    }

    override fun getUnsyncedTasksFlow(): Flow<List<TaskDetails>> {
        return taskOutboxDao.getOutboxFlow().map { outboxEntities ->
            val members = memberDao.get().associateBy { it.id }
            val houses = houseDao.get().associateBy { it.id }

            outboxEntities.map { outboxItem ->
                val taskId = outboxItem.taskId ?: outboxItem.id
                val task = Task(
                    id = taskId,
                    name = outboxItem.name ?: "",
                    description = outboxItem.description ?: "",
                    dueDateTime = outboxItem.dueDateTime ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
                    repeatValue = outboxItem.repeatValue?.toInt() ?: 0,
                    repeatUnit = outboxItem.repeatUnit ?: RepeatUnit.NONE,
                    repeatEndDateTime = outboxItem.repeatEndDateTime,
                    houseId = outboxItem.houseId ?: "",
                    memberId = outboxItem.memberId ?: "",
                    rotateMember = outboxItem.rotateMember ?: false,
                    createdDate = outboxItem.createdDate,
                    status = outboxItem.status ?: ActiveStatus.ACTIVE
                )
                val memberName = members[task.memberId]?.name ?: ""
                val houseName = houses[task.houseId]?.name ?: ""
                val isSharedList = memberHouseAssociationDao.getForHouse(task.houseId).size > 1
                TaskDetails(
                    task = task,
                    memberName = memberName,
                    houseName = houseName,
                    isSharedList = isSharedList,
                    isSyncPending = true
                )
            }
        }
    }

    override suspend fun getTask(id: String): Task? {
        val taskEntity = taskDao.get(id) ?: return null
        return Task(taskEntity)
    }

    @OptIn(ExperimentalUuidApi::class)
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
        val existingTask = taskDao.get(taskId) ?: return Result.Failure(EditTaskError.TaskNotFound)
        val outboxId = Uuid.random().toString()

        val outboxEntity = TaskOutboxEntity(
            id = outboxId,
            type = "edit",
            taskId = taskId,
            name = name ?: existingTask.name,
            description = description ?: existingTask.description,
            dueDateTime = dueDateTime ?: existingTask.dueDateTime,
            repeatValue = repeatValue ?: existingTask.repeatValue,
            repeatUnit = repeatUnit ?: existingTask.repeatUnit,
            repeatEndDateTime = repeatEndDateTime ?: existingTask.repeatEndDateTime,
            houseId = existingTask.houseId,
            memberId = existingTask.memberId,
            rotateMember = rotateMember ?: existingTask.rotateMember,
            createdDate = Clock.System.now(),
            status = status ?: existingTask.status
        )

        logger.v(TAG, "Edit task requested: $outboxEntity")
        taskOutboxDao.insert(outboxEntity)
        taskUploadRequestHandler.requestDelayedUpload()
        return Result.Success(Unit)
    }

    @OptIn(ExperimentalUuidApi::class)
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
        val outboxId = Uuid.random().toString()
        val outboxEntity = TaskOutboxEntity(
            id = outboxId,
            type = "add",
            taskId = null,
            name = name,
            description = description,
            dueDateTime = dueDateTime,
            repeatValue = repeatValue,
            repeatUnit = repeatUnit,
            repeatEndDateTime = repeatEndDateTime,
            houseId = houseId,
            memberId = memberId,
            rotateMember = rotateMember,
            createdDate = Clock.System.now(),
            status = ActiveStatus.ACTIVE
        )

        logger.v(TAG, "Add task requested: $outboxEntity")
        taskOutboxDao.insert(outboxEntity)
        taskUploadRequestHandler.requestDelayedUpload()
        return Result.Success(Unit)
    }

    companion object {
        private const val TAG = "TasksRepository"
    }
}

interface TasksRepository {

    suspend fun fetchTasks(): Result<Unit>

    suspend fun uploadLocal(): List<String>

    fun getTasksFlow(): Flow<List<TaskDetails>>

    fun getUnsyncedTasksFlow(): Flow<List<TaskDetails>>

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
