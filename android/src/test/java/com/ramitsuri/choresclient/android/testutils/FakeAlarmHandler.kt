package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.android.data.AlarmEntity
import com.ramitsuri.choresclient.android.data.AssignmentAlarm
import com.ramitsuri.choresclient.android.reminder.AlarmHandler
import java.time.Instant

class FakeAlarmHandler : AlarmHandler {
    private val alarms = mutableMapOf<String, AssignmentAlarm>() // AssignmentId | DueDateTime

    override suspend fun getExisting(): List<AlarmEntity> {
        return alarms.values.map {
            AlarmEntity(
                it.assignmentId,
                it.showAtTime,
                it.systemNotificationId
            )
        }
    }

    override suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>) {
        assignmentAlarms.forEach { assignmentAlarm ->
            alarms[assignmentAlarm.assignmentId] =
                AssignmentAlarm(
                    assignmentAlarm.assignmentId,
                    assignmentAlarm.showAtTime,
                    assignmentAlarm.systemNotificationId,
                    assignmentAlarm.systemNotificationText
                )

        }
    }

    override suspend fun reschedule(
        assignmentId: String,
        showAtTime: Instant,
        notificationText: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun cancel(assignmentIds: List<String>) {
        assignmentIds.forEach { assignmentId ->
            alarms.remove(assignmentId)
        }
    }


    fun get(assignmentId: String): AssignmentAlarm? = alarms[assignmentId]
}