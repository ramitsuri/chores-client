package com.ramitsuri.choresclient.android.reminder

import com.ramitsuri.choresclient.android.data.AlarmEntity
import com.ramitsuri.choresclient.android.data.AssignmentAlarm

interface AlarmHandler {
    suspend fun getExisting(): List<AlarmEntity>

    suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>)

    suspend fun cancel(assignmentIds: List<String>)
}