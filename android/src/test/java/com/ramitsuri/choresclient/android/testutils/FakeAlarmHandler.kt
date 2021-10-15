package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.android.data.ReminderAssignment
import com.ramitsuri.choresclient.android.reminder.AlarmHandler

class FakeAlarmHandler: AlarmHandler {
    private val alarms = mutableMapOf<Long, Int>() // Time | RequestCode
    override fun schedule(reminder: ReminderAssignment) {
        alarms[reminder.time] = reminder.requestCode
    }

    override fun cancel(reminder: ReminderAssignment) {
        alarms.remove(reminder.time)
    }

    fun scheduledForTime(time: Long): Boolean {
        return alarms[time] != null
    }

    fun getScheduledTimes(): List<Long> {
        return alarms.keys.toList()
    }
}