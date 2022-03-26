package com.ramitsuri.choresclient.android.reminder

import android.content.Context
import com.ramitsuri.choresclient.android.data.AlarmDao
import com.ramitsuri.choresclient.android.data.AlarmEntity
import com.ramitsuri.choresclient.android.data.AssignmentAlarm
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker
import java.time.Instant

class SystemAlarmHandler(
    private val showNotificationWorker: ShowNotificationWorker.Companion,
    private val alarmDao: AlarmDao,
    context: Context
) : AlarmHandler {
    private val context = context.applicationContext

    override suspend fun getExisting(): List<AlarmEntity> {
        return alarmDao.get()
    }

    override suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>) {
        showNotificationWorker.schedule(context, assignmentAlarms)
        alarmDao.insert(assignmentAlarms.map {
            AlarmEntity(
                it.assignmentId,
                it.showAtTime,
                it.systemNotificationId
            )
        })
    }

    override suspend fun reschedule(
        assignmentId: String,
        showAtTime: Instant,
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
                    existing.systemNotificationId,
                    notificationText
                )
            )
        )
    }

    override suspend fun cancel(assignmentIds: List<String>) {
        showNotificationWorker.cancel(context, assignmentIds)
        alarmDao.delete(assignmentIds.map { AlarmEntity(it) })
    }
}