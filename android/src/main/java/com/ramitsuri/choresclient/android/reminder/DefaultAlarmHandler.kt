package com.ramitsuri.choresclient.android.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAlarmHandler(
    private val notificationManager: NotificationManager,
    private val alarmDao: AlarmDao,
    context: Context
) : AlarmHandler, KoinComponent {

    private val logger: LogHelper by inject()
    private val assignmentRepo: TaskAssignmentsRepository by inject()
    private val prefManager: PrefManager by inject()
    private val context = context.applicationContext
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
        scheduleNextAlarm()
    }

    private suspend fun processDueAlarms(dueAlarms: List<AlarmEntity>) {
        logger.v(TAG, "Processing ${dueAlarms.size} due alarms")
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
                    channelId = context.getString(R.string.notification_reminders_id),
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
        }
    }

    private suspend fun scheduleNextAlarm() {
        val now = LocalDateTime.now()
        val allAlarms = alarmDao.get()

        val dueAlarms = allAlarms.filter { it.showAtTime <= now }
        if (dueAlarms.isNotEmpty()) {
            processDueAlarms(dueAlarms)
        }

        val remainingAlarms = alarmDao.get()
        val nextAlarm = remainingAlarms
            .filter { it.showAtTime > now }
            .minByOrNull { it.showAtTime }

        val pendingIntent = getAlarmPendingIntent()

        if (nextAlarm != null) {
            val triggerAtMillis = nextAlarm.showAtTime
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()

            logger.v(
                TAG,
                "Scheduling next alarm for assignment ${nextAlarm.assignmentId} at ${nextAlarm.showAtTime} ($triggerAtMillis)"
            )
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            logger.v(TAG, "No upcoming alarms. Cancelling single AlarmManager alarm")
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun getAlarmPendingIntent(): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SHOW_NOTIFICATION
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "AlarmHandler"
        private const val REQUEST_CODE_ALARM = 1001
    }
}
