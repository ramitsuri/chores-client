package com.ramitsuri.choresclient.android.reminder

import android.content.Context
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker
import com.ramitsuri.choresclient.data.db.dao.AlarmDao
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.model.entities.AssignmentAlarm
import com.ramitsuri.choresclient.notification.NotificationManager
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class DefaultAlarmHandler(
    private val showNotificationWorker: ShowNotificationWorker.Companion,
    private val notificationManager: NotificationManager,
    private val alarmDao: AlarmDao,
    context: Context
) : AlarmHandler, KoinComponent {
    private val logger: LogHelper by inject()
    private val context = context.applicationContext

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
        showNotificationWorker.schedule(context, assignmentAlarms)
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
        showNotificationWorker.cancel(context, assignmentIds)
        val existingToCancel = getExisting().filter { assignmentIds.contains(it.assignmentId) }
        existingToCancel.forEach { existingAlarm ->
            notificationManager.cancelNotification(existingAlarm.systemNotificationId.toInt())
        }
        alarmDao.delete(assignmentIds)
    }

    companion object {
        private const val TAG = "AlarmHandler"
    }
}