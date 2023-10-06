package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.db.dao.MemberDao
import com.ramitsuri.choresclient.data.db.dao.TaskAssignmentDao
import com.ramitsuri.choresclient.data.db.dao.TaskDao
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.entities.TaskAssignmentUpdate
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.network.api.TaskAssignmentsApi
import com.ramitsuri.choresclient.network.model.toTaskAssignmentEntity
import com.ramitsuri.choresclient.network.model.toTaskEntity
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.reminder.ReminderScheduler
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.getNewReminderTimeSnoozeDay
import com.ramitsuri.choresclient.utils.getNewReminderTimeSnoozeHour
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("MoveVariableDeclarationIntoWhen")
class DefaultTaskAssignmentsRepository(
    private val api: TaskAssignmentsApi,
    private val taskAssignmentDao: TaskAssignmentDao,
    private val memberDao: MemberDao,
    private val taskDao: TaskDao,
    private val reminderScheduler: ReminderScheduler,
    private val alarmHandler: AlarmHandler,
    private val contentDownloadRequestHandler: ContentDownloadRequestHandler,
) : TaskAssignmentsRepository, KoinComponent {

    private val logger: LogHelper by inject()
    override suspend fun refresh(): Result<Unit> {
        // Upload completed local assignments
        val uploadedIds = uploadLocal()

        // Delete assignments that have been confirmed to be uploaded
        taskAssignmentDao.delete(uploadedIds)

        // Fetch from server and save locally
        val fetchAndSaveResult = fetchAndSave()
        return when (fetchAndSaveResult) {
            is Result.Failure -> {
                fetchAndSaveResult
            }

            is Result.Success -> {
                reminderScheduler.scheduleReminders(fetchAndSaveResult.data)
                Result.Success(Unit)
            }
        }
    }

    override suspend fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> {
        return taskAssignmentDao.getAllFlow()
            .combine(alarmHandler.getExistingFlow()) { assignmentEntities, alarmEntities ->
                val assignmentDetails = mutableListOf<TaskAssignmentDetails>()
                assignmentEntities.forEach { assignmentEntity ->
                    val assignment = toTaskAssignment(assignmentEntity) ?: return@forEach
                    val alarmEntity = alarmEntities
                        .find { it.assignmentId == assignment.id }
                    assignmentDetails.add(
                        TaskAssignmentDetails(
                            taskAssignment = assignment,
                            reminderTime = alarmEntity?.showAtTime,
                            enableSnooze = loggedInMemberId == assignment.memberId,
                            willReminderBeSet = if (alarmEntity?.showAtTime != null) {
                                true
                            } else {
                                assignment.repeatInfo.repeatUnit != RepeatUnit.ON_COMPLETE &&
                                        assignment.memberId == loggedInMemberId
                            }
                        )
                    )
                }
                assignmentDetails
            }
    }

    override suspend fun getLocal(id: String): TaskAssignment? {
        return toTaskAssignment(taskAssignmentDao.get(id))
    }

    override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {
        val taskAssignment = TaskAssignmentUpdate(
            taskAssignmentId,
            ProgressStatus.DONE,
            doneTime,
            shouldUpload = true
        )
        taskAssignmentDao.update(taskAssignment)
        alarmHandler.cancel(listOf(taskAssignmentId))
        contentDownloadRequestHandler.requestDelayedDownload()
    }

    override suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant) {
        val taskAssignment = TaskAssignmentUpdate(
            taskAssignmentId,
            ProgressStatus.WONT_DO,
            wontDoTime,
            shouldUpload = true
        )
        taskAssignmentDao.update(taskAssignment)
        alarmHandler.cancel(listOf(taskAssignmentId))
        contentDownloadRequestHandler.requestDelayedDownload()
    }

    override suspend fun onSnoozeHourRequested(
        assignmentId: String,
    ) {
        val showAtTime = getNewReminderTimeSnoozeHour()
        alarmHandler.reschedule(assignmentId, showAtTime)
    }

    override suspend fun onSnoozeDayRequested(
        assignmentId: String,
    ) {
        val showAtTime = getNewReminderTimeSnoozeDay()
        alarmHandler.reschedule(assignmentId, showAtTime)
    }

    private suspend fun uploadLocal(): List<String> {
        val readyForUpload = toTaskAssignments(taskAssignmentDao.getForUpload())
        logger.v(TAG, "Ready for upload: ${readyForUpload.joinToString()}")
        if (readyForUpload.isEmpty()) {
            return listOf()
        }

        val uploadResult = api.updateTaskAssignments(readyForUpload)
        return when (uploadResult) {
            is Result.Failure -> {
                listOf()
            }

            is Result.Success -> {
                val uploadedTaskAssignmentIds: List<String> = uploadResult.data
                logger.v(TAG, "Uploaded: ${uploadedTaskAssignmentIds.joinToString()}")
                uploadedTaskAssignmentIds
            }
        }
    }

    private suspend fun fetchAndSave(): Result<List<TaskAssignment>> {
        val result = api.getTaskAssignments()

        return when (result) {
            is Result.Failure -> {
                Result.Failure(result.error)
            }

            is Result.Success -> {
                result.data
                    .map {
                        it.task.toTaskEntity()
                    }.also { tasks ->
                        taskDao.clearAndInsert(tasks)
                    }

                val taskAssignmentEntities = result.data
                    .map {
                        it.toTaskAssignmentEntity(shouldUpload = false)
                    }
                taskAssignmentDao.clearTodoAndInsert(taskAssignmentEntities)

                logger.v(TAG, "Fetched: ${result.data.joinToString { it.id }}")
                Result.Success(toTaskAssignments(taskAssignmentEntities))
            }
        }
    }

    private suspend fun toTaskAssignments(
        taskAssignmentEntities: List<TaskAssignmentEntity>
    ): List<TaskAssignment> {
        return taskAssignmentEntities.mapNotNull { assignmentEntity ->
            toTaskAssignment(assignmentEntity)
        }
    }

    private suspend fun toTaskAssignment(assignmentEntity: TaskAssignmentEntity?): TaskAssignment? {
        if (assignmentEntity == null) {
            return null
        }

        val memberEntity = memberDao.get(assignmentEntity.memberId)
        val taskEntity = taskDao.get(assignmentEntity.taskId)

        return if (taskEntity == null || memberEntity == null) {
            null
        } else {
            TaskAssignment.fromEntities(
                assignmentEntity,
                taskEntity,
                memberEntity
            )
        }
    }

    companion object {
        private const val TAG = "TaskAssignmentsRepository"
    }
}

interface TaskAssignmentsRepository {
    suspend fun refresh(): Result<Unit>

    suspend fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>>

    suspend fun getLocal(id: String): TaskAssignment?

    suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant)

    suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant)

    suspend fun onSnoozeHourRequested(assignmentId: String)

    suspend fun onSnoozeDayRequested(assignmentId: String)

}