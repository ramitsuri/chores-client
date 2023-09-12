package com.ramitsuri.choresclient.reminder

import com.ramitsuri.choresclient.model.entities.AssignmentAlarm
import com.ramitsuri.choresclient.db.AlarmEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface AlarmHandler {
    suspend fun getExisting(): List<AlarmEntity>

    fun getExistingFlow(): Flow<List<AlarmEntity>>

    suspend fun getExisting(assignmentId: String): AlarmEntity?

    suspend fun schedule(assignmentAlarms: List<AssignmentAlarm>)

    suspend fun reschedule(assignmentId: String, showAtTime: LocalDateTime)

    suspend fun cancel(assignmentIds: List<String>)
}