package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.data.entities.AssignmentAlarm
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.reminder.AlarmHandler
import kotlinx.datetime.LocalDateTime

class FakeAlarmHandler : AlarmHandler {
    private val alarms = mutableMapOf<String, AssignmentAlarm>() // AssignmentId | DueDateTime

    override suspend fun getExisting(): List<AlarmEntity> {
        return alarms.values.map {
            AlarmEntity(
                it.assignmentId,
                it.showAtTime,
                it.systemNotificationId.toLong()
            )
        }
    }

    override suspend fun getExisting(assignmentId: String): AlarmEntity? {
        return alarms.values.filter {
            it.assignmentId == assignmentId
        }.map {
            AlarmEntity(
                it.assignmentId,
                it.showAtTime,
                it.systemNotificationId.toLong()
            )
        }.firstOrNull()
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
        showAtTime: LocalDateTime,
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