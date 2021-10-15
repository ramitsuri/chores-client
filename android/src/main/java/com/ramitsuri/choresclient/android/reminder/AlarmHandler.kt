package com.ramitsuri.choresclient.android.reminder

import com.ramitsuri.choresclient.android.data.ReminderAssignment

interface AlarmHandler {

    fun schedule(reminder: ReminderAssignment)

    fun cancel(reminder: ReminderAssignment)
}