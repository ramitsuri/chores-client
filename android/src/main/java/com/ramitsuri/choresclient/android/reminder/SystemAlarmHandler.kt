package com.ramitsuri.choresclient.android.reminder

import android.content.Context
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker
import com.ramitsuri.choresclient.data.entities.AlarmDao
import com.ramitsuri.choresclient.data.entities.AssignmentAlarm
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class SystemAlarmHandler(
    private val showNotificationWorker: ShowNotificationWorker.Companion,
    private val alarmDao: AlarmDao,
    context: Context
) : AlarmHandler, KoinComponent {
    private val logger: LogHelper by inject()
    private val context = context.applicationContext

    override suspend fun getExisting(): List<AlarmEntity> {
        return alarmDao.get()
    }

    override suspend fun getExisting(assignmentId: String): AlarmEntity? {
        return alarmDao.get(assignmentId)
    }

    override suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>) {
        showNotificationWorker.schedule(context, assignmentAlarms)
        alarmDao.insert(assignmentAlarms.map {
            AlarmEntity(
                it.assignmentId,
                it.showAtTime,
                it.systemNotificationId.toLong()
            )
        })
    }

    override suspend fun reschedule(
        assignmentId: String,
        showAtTime: LocalDateTime,
        notificationText: String
    ) {
        val existing = alarmDao.get(assignmentId) ?: return
        // Cancel existing notification
        cancel(listOf(assignmentId))
        schedule(
            listOf(
                AssignmentAlarm(
                    assignmentId,
                    showAtTime,
                    existing.systemNotificationId.toInt(),
                    notificationText
                )
            )
        )
    }

    override suspend fun cancel(assignmentIds: List<String>) {
        logger.v(TAG, "Cancel requested for $assignmentIds")
        showNotificationWorker.cancel(context, assignmentIds)
        alarmDao.delete(assignmentIds)
    }

    companion object {
        private const val TAG = "SystemAlarmHandler"
    }
}