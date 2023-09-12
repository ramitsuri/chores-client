package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.model.entities.AssignmentAlarm
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.reminder.AlarmHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

class FakeAlarmHandler : AlarmHandler {
    private val alarms = mutableMapOf<String, AssignmentAlarm>() // AssignmentId | DueDateTime

    override suspend fun getExisting(): List<AlarmEntity> {
        return alarms.values.map {
            AlarmEntity(
                assignmentId = it.assignmentId,
                showAtTime = it.showAtTime,
                systemNotificationId = it.systemNotificationId.toLong()
            )
        }
    }

    override suspend fun getExisting(assignmentId: String): AlarmEntity? {
        return alarms.values.filter {
            it.assignmentId == assignmentId
        }.map {
            AlarmEntity(
                assignmentId = it.assignmentId,
                showAtTime = it.showAtTime,
                systemNotificationId = it.systemNotificationId.toLong()
            )
        }.firstOrNull()
    }

    override fun getExistingFlow(): Flow<List<AlarmEntity>> {
        TODO("Not yet implemented")
    }


    override suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>) {
        assignmentAlarms.forEach { assignmentAlarm ->
            alarms[assignmentAlarm.assignmentId] =
                AssignmentAlarm(
                    assignmentAlarm.assignmentId,
                    assignmentAlarm.showAtTime,
                    assignmentAlarm.systemNotificationId,
                )

        }
    }

    override suspend fun reschedule(assignmentId: String, showAtTime: LocalDateTime) {
        TODO("Not yet implemented")
    }

    override suspend fun cancel(assignmentIds: List<String>) {
        assignmentIds.forEach { assignmentId ->
            alarms.remove(assignmentId)
        }
    }

    fun get(assignmentId: String): AssignmentAlarm? = alarms[assignmentId]
}