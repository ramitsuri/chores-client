package com.ramitsuri.choresclient.android.reminder

import com.ramitsuri.choresclient.android.model.TaskAssignment

interface AlarmHandler {
    fun schedule(taskAssignment: TaskAssignment)

    fun cancel(taskAssignment: TaskAssignment)
}