package com.ramitsuri.choresclient.android.reminder

import android.content.Context
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker

class SystemAlarmHandler(
    private val showNotificationWorker: ShowNotificationWorker.Companion,
    context: Context
) : AlarmHandler {
    private val context = context.applicationContext

    override fun schedule(taskAssignment: TaskAssignment) {
        showNotificationWorker.schedule(context, taskAssignment)
    }

    override fun cancel(taskAssignment: TaskAssignment) {
        showNotificationWorker.cancel(context, taskAssignment)
    }
}