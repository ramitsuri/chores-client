package com.ramitsuri.choresclient.android.reminder

import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.model.DeepLink
import com.ramitsuri.choresclient.android.notification.AssignmentActionReceiver
import com.ramitsuri.choresclient.android.uriWithArgsValues
import com.ramitsuri.choresclient.android.utils.NotificationAction
import com.ramitsuri.choresclient.android.utils.NotificationActionExtra
import com.ramitsuri.choresclient.data.db.dao.AlarmDao
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.model.entities.AssignmentAlarm
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.notification.NotificationActionInfo
import com.ramitsuri.choresclient.notification.NotificationInfo
import com.ramitsuri.choresclient.notification.NotificationManager
import com.ramitsuri.choresclient.notification.Priority
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.now
import com.ramitsuri.choresclient.utils.plus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAlarmHandler(
    private val notificationManager: NotificationManager,
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler,
    private val channelId: String
) : AlarmHandler, KoinComponent {

    private val logger: LogHelper by inject()
    private val assignmentRepo: TaskAssignmentsRepository by inject()
    private val prefManager: PrefManager by inject()

    override suspend fun getExisting(): List<AlarmEntity> {
        return alarmDao.get()
    }

    override fun getExistingFlow(): Flow<List<AlarmEntity>> {
        return alarmDao.getFlow()
    }

    override suspend fun getExisting(assignmentId: String): AlarmEntity? {
        return alarmDao.get(assignmentId)
    }

    override suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>) {
        // Cancel existing notification
        cancel(assignmentAlarms.map { it.assignmentId })
        alarmDao.insert(assignmentAlarms.map {
            AlarmEntity(
                systemNotificationId = it.systemNotificationId.toLong(),
                assignmentId = it.assignmentId,
                showAtTime = it.showAtTime,
            )
        })
        scheduleNextAlarm()
    }

    override suspend fun reschedule(
        assignmentId: String,
        showAtTime: LocalDateTime,
    ) {
        val existing = alarmDao.get(assignmentId) ?: return
        schedule(
            listOf(
                AssignmentAlarm(
                    assignmentId,
                    showAtTime,
                    existing.systemNotificationId.toInt(),
                )
            )
        )
    }

    override suspend fun cancel(assignmentIds: List<String>) {
        logger.v(TAG, "Cancel requested for $assignmentIds")
        val existingToCancel = getExisting().filter { assignmentIds.contains(it.assignmentId) }
        existingToCancel.forEach { existingAlarm ->
            notificationManager.cancelNotification(existingAlarm.systemNotificationId.toInt())
        }
        alarmDao.delete(assignmentIds)
        scheduleNextAlarm()
    }

    override suspend fun onAlarmFired() {
        val now = LocalDateTime.now()
        val allAlarms = alarmDao.get()

        val dueAlarms = allAlarms.filter { it.showAtTime <= now }
        if (dueAlarms.isNotEmpty()) {
            logger.v(TAG, "onAlarmFired: Processing ${dueAlarms.size} due alarms")
            processDueAlarms(dueAlarms)
        }

        scheduleNextAlarm()
    }

    private suspend fun processDueAlarms(dueAlarms: List<AlarmEntity>) {
        for (alarmEntity in dueAlarms) {
            val assignment = assignmentRepo.getLocal(alarmEntity.assignmentId)
            if (assignment == null) {
                logger.v(TAG, "Assignment ${alarmEntity.assignmentId} null. Skipping notification")
                continue
            }
            if (assignment.progressStatus != ProgressStatus.TODO) {
                logger.v(
                    TAG,
                    "Assignment ${alarmEntity.assignmentId} not TODO (${assignment.progressStatus}). Skipping notification"
                )
                continue
            }

            val notificationId = alarmEntity.systemNotificationId.toInt()
            val notificationActions = prefManager.getEnabledNotificationActions().map {
                val action = NotificationAction.fromAction(it)
                NotificationActionInfo(
                    action.action,
                    action.text,
                    AssignmentActionReceiver::class
                )
            }

            notificationManager.showNotification(
                NotificationInfo(
                    id = notificationId,
                    channelId = channelId,
                    priority = Priority.HIGH,
                    title = assignment.taskName,
                    body = null,
                    additionalText = null,
                    iconResId = R.drawable.ic_notification,
                    actions = notificationActions,
                    actionExtras = mapOf(
                        NotificationActionExtra.KEY_ASSIGNMENT_ID to alarmEntity.assignmentId,
                    ),
                    clickDeepLinkUri = DeepLink.ASSIGNMENT.uriWithArgsValues(listOf(alarmEntity.assignmentId))
                )
            )

            // Advance showAtTime by 1 day in AlarmDao so that if the task remains TODO,
            // the next reminder fires 24 hours later automatically
            val nextRepeatTime = alarmEntity.showAtTime.plus(days = 1)
            alarmDao.insert(
                listOf(
                    AlarmEntity(
                        systemNotificationId = alarmEntity.systemNotificationId,
                        assignmentId = alarmEntity.assignmentId,
                        showAtTime = nextRepeatTime
                    )
                )
            )
            logger.v(TAG, "Advanced showAtTime for ${alarmEntity.assignmentId} to $nextRepeatTime")
        }
    }

    private suspend fun scheduleNextAlarm() {
        val now = LocalDateTime.now()
        val allAlarms = alarmDao.get()

        val nextAlarm = allAlarms
            .filter { it.showAtTime > now }
            .minByOrNull { it.showAtTime }

        if (nextAlarm != null) {
            logger.v(
                TAG,
                "Scheduling next alarm for assignment ${nextAlarm.assignmentId} at ${nextAlarm.showAtTime}"
            )
            alarmScheduler.scheduleAlarm(nextAlarm.showAtTime)
        } else {
            logger.v(TAG, "No upcoming alarms. Cancelling single AlarmManager alarm")
            alarmScheduler.cancelAlarm()
        }
    }

    companion object {
        private const val TAG = "AlarmHandler"
    }
}
