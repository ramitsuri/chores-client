package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.db.dao.MemberDao
import com.ramitsuri.choresclient.data.db.dao.MemberHouseAssociationDao
import com.ramitsuri.choresclient.data.db.dao.TaskAssignmentDao
import com.ramitsuri.choresclient.data.db.dao.TaskDao
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.entities.TaskAssignmentUpdate
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.network.api.TaskAssignmentsApi
import com.ramitsuri.choresclient.network.model.toTaskAssignmentEntity
import com.ramitsuri.choresclient.network.model.toTaskEntity
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.reminder.ReminderScheduler
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.getNewReminderTimeSnoozeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Suppress("MoveVariableDeclarationIntoWhen")
class DefaultTaskAssignmentsRepository(
    private val api: TaskAssignmentsApi,
    private val taskAssignmentDao: TaskAssignmentDao,
    private val memberDao: MemberDao,
    private val taskDao: TaskDao,
    private val memberHouseAssociationDao: MemberHouseAssociationDao,
    private val reminderScheduler: ReminderScheduler,
    private val alarmHandler: AlarmHandler,
    private val contentDownloadRequestHandler: ContentDownloadRequestHandler,
) : TaskAssignmentsRepository, KoinComponent {

    private val logger: LogHelper by inject()
    override suspend fun refresh(forceRemindPastDue: Boolean, forceRemindFuture: Boolean) {
        // Upload completed local assignments
        val uploadedIds = uploadLocal()

        // Mark assignments that have been confirmed to be uploaded
        taskAssignmentDao.markUploaded(uploadedIds)

        // Fetch from server and save locally
        fetchAndSave()

        // Schedule reminder for all locally available
        reminderScheduler.scheduleReminders(
            toTaskAssignments(taskAssignmentDao.getAll()
                .filter { it.progressStatus == ProgressStatus.TODO }
            ),
            forceRemindPastDue = forceRemindPastDue,
            forceRemindFuture = forceRemindFuture,
        )
    }

    override fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> {
        return taskAssignmentDao.getTodoFlow()
            .combine(alarmHandler.getExistingFlow()) { assignmentEntities, alarmEntities ->
                val assignmentDetails = mutableListOf<TaskAssignmentDetails>()
                assignmentEntities.forEach { assignmentEntity ->
                    val assignment = toTaskAssignment(assignmentEntity) ?: return@forEach
                    val alarmEntity = alarmEntities
                        .find { it.assignmentId == assignment.id }
                    val isSharedList =
                        memberHouseAssociationDao.getForHouse(assignment.houseId).size > 1
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
                            },
                            assignedToLoggedInUser = assignment.memberId == loggedInMemberId,
                            isSharedList = isSharedList
                        )
                    )
                }
                assignmentDetails
            }
    }

    override fun getCompletedLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> {
        return taskAssignmentDao.getCompletedFlow()
            .map { assignmentEntities ->
                val assignmentDetails = mutableListOf<TaskAssignmentDetails>()
                assignmentEntities.forEach { assignmentEntity ->
                    val assignment = toTaskAssignment(assignmentEntity) ?: return@forEach
                    val isSharedList =
                        memberHouseAssociationDao.getForHouse(assignment.houseId).size > 1
                    assignmentDetails.add(
                        TaskAssignmentDetails(
                            taskAssignment = assignment,
                            reminderTime = null,
                            enableSnooze = false,
                            willReminderBeSet = false,
                            assignedToLoggedInUser = assignment.memberId == loggedInMemberId,
                            isSharedList = isSharedList
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

    override suspend fun transfer(taskAssignmentId: String) {
        taskAssignmentDao.markTransfer(taskAssignmentId)
        contentDownloadRequestHandler.requestDelayedDownload()
    }

    override suspend fun onSnoozeRequested(
        assignmentId: String,
        snoozeType: SnoozeType,
    ) {
        val showAtTime = getNewReminderTimeSnoozeType(snoozeType)
        alarmHandler.reschedule(assignmentId, showAtTime)
    }

    override suspend fun fetchAndSaveCompleted() {
        val result = api.getTaskAssignments(
            progressStatus = listOf(ProgressStatus.DONE, ProgressStatus.WONT_DO),
            dueDateWithinDays = COMPLETED_WITHIN_DAYS_COUNT,
        )

        if (result is Result.Success) {
            result.data
                .map {
                    it.task.toTaskEntity()
                }.also { tasks ->
                    taskDao.insert(tasks)
                }

            val taskAssignmentEntities = result.data
                .map {
                    it.toTaskAssignmentEntity(shouldUpload = false)
                }
            taskAssignmentDao.insert(taskAssignmentEntities)

            logger.v(TAG, "Fetched completed: ${result.data.joinToString { it.id }}")
        }
    }

    override suspend fun deleteOldCompleted(now: Instant) {
        val daysAgo = now.minus(COMPLETED_WITHIN_DAYS_COUNT.days)
        taskAssignmentDao.deleteOldCompleted(daysAgo)
    }

    private suspend fun uploadLocal(): List<String> {
        val readyForUploadEntities = taskAssignmentDao.getForUpload()
        logger.v(TAG, "Ready for upload: ${readyForUploadEntities.joinToString { it.id }}")
        if (readyForUploadEntities.isEmpty()) {
            return listOf()
        }

        val updates = readyForUploadEntities.map { entity ->
            TaskAssignmentUpdate(
                id = entity.id,
                progressStatus = entity.progressStatus,
                progressStatusDate = entity.progressStatusDate,
                shouldUpload = true,
                shouldTransfer = entity.shouldTransfer
            )
        }

        val uploadResult = api.updateTaskAssignments(updates)
        return when (uploadResult) {
            is Result.Failure -> {
                listOf()
            }

            is Result.Success -> {
                val uploadedTaskAssignmentIds: List<String> = uploadResult.data
                logger.v(TAG, "Uploaded task assignments: ${uploadedTaskAssignmentIds.joinToString()}")
                uploadedTaskAssignmentIds
            }
        }
    }

    private suspend fun fetchAndSave() {
        val result = api.getTaskAssignments()

        if (result is Result.Success) {
            result.data
                .map {
                    it.task.toTaskEntity()
                }.also { tasks ->
                    taskDao.insert(tasks)
                }

            val taskAssignmentEntities = result.data
                .map {
                    it.toTaskAssignmentEntity(shouldUpload = false)
                }
            taskAssignmentDao.clearTodoAndInsert(taskAssignmentEntities)

            logger.v(TAG, "Fetched: ${result.data.joinToString { it.id }}")
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
        private const val COMPLETED_WITHIN_DAYS_COUNT = 100
    }
}

interface TaskAssignmentsRepository {
    suspend fun refresh(forceRemindPastDue: Boolean = false, forceRemindFuture: Boolean = false)

    suspend fun fetchAndSaveCompleted()

    suspend fun deleteOldCompleted(now: Instant = Clock.System.now())

    fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>>

    fun getCompletedLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>>

    suspend fun getLocal(id: String): TaskAssignment?

    suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant)

    suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant)

    suspend fun transfer(taskAssignmentId: String)

    suspend fun onSnoozeRequested(assignmentId: String, snoozeType: SnoozeType)
}
