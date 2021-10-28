package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.android.reminder.AlarmHandler

class FakeAlarmHandler: AlarmHandler {
    private val alarms = mutableMapOf<Long, Int>() // Time | RequestCode
    override fun schedule(requestCode: Int, time: Long) {
        alarms[time] = requestCode
    }

    override fun cancel(requestCode: Int) {
        val filteredAlarms =
            alarms.filter {(alarmTime, alarmRequestCode) -> alarmRequestCode == requestCode}
        for ((key, value) in filteredAlarms) {
            alarms.remove(key)
        }
    }

    fun scheduledForTime(time: Long): Boolean {
        return alarms[time] != null
    }

    fun getScheduledTimes(): List<Long> {
        return alarms.keys.toList()
    }
}