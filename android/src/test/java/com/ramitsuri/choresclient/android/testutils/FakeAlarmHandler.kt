package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.reminder.AlarmHandler

class FakeAlarmHandler : AlarmHandler {
    private val alarms = mutableMapOf<String, Long>() // AssignmentId | DueDateTime

    override fun schedule(taskAssignment: TaskAssignment) {
        alarms[taskAssignment.id] = taskAssignment.dueDateTime.toEpochMilli()
    }

    override fun cancel(taskAssignment: TaskAssignment) {
        alarms.remove(taskAssignment.id)
    }

    fun get(assignmentId: String): Long? = alarms[assignmentId]
}