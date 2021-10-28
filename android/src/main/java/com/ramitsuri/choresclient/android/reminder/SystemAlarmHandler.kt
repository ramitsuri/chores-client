package com.ramitsuri.choresclient.android.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.android.data.ReminderAssignment

class SystemAlarmHandler(context: Context): AlarmHandler {
    private val context = context.applicationContext

    override fun schedule(requestCode: Int, time: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            generatePendingIntent(requestCode)
        )
    }

    override fun cancel(requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.cancel(generatePendingIntent(requestCode))
    }

    private fun generatePendingIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.action = "REMINDER_CODE=${requestCode}"
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}