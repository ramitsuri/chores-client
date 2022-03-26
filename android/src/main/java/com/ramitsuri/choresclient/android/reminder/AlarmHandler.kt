package com.ramitsuri.choresclient.android.reminder

import com.ramitsuri.choresclient.android.data.AlarmEntity
import com.ramitsuri.choresclient.android.data.AssignmentAlarm
import java.time.Instant

interface AlarmHandler {
    suspend fun getExisting(): List<AlarmEntity>

    suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>)

    suspend fun reschedule(assignmentId: String, showAtTime: Instant, notificationText: String)

    suspend fun cancel(assignmentIds: List<String>)
}