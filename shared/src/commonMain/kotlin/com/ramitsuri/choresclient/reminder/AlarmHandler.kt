package com.ramitsuri.choresclient.reminder

import com.ramitsuri.choresclient.data.entities.AssignmentAlarm
import com.ramitsuri.choresclient.db.AlarmEntity
import kotlinx.datetime.Instant

interface AlarmHandler {
    suspend fun getExisting(): List<AlarmEntity>

    suspend fun getExisting(assignmentId: String): AlarmEntity?

    suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>)

    suspend fun reschedule(assignmentId: String, showAtTime: Instant, notificationText: String)

    suspend fun cancel(assignmentIds: List<String>)
}