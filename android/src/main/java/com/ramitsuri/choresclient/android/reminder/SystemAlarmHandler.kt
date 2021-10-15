package com.ramitsuri.choresclient.android.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.android.data.ReminderAssignment

class SystemAlarmHandler(context: Context): AlarmHandler {
    private val context = context.applicationContext

    override fun schedule(reminder: ReminderAssignment) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.time,
            generatePendingIntent(reminder)
        )
    }

    override fun cancel(reminder: ReminderAssignment) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.cancel(generatePendingIntent(reminder))
    }

    private fun generatePendingIntent(reminder: ReminderAssignment): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.action = "REMINDER_CODE=${reminder.requestCode}"
        return PendingIntent.getBroadcast(
            context,
            reminder.requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}