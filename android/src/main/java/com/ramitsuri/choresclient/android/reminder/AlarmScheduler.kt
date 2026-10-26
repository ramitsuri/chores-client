package com.ramitsuri.choresclient.android.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AlarmScheduler {
    fun scheduleAlarm(showAtTime: LocalDateTime)
    fun cancelAlarm()
}

class AndroidAlarmScheduler(
    context: Context,
) : AlarmScheduler, KoinComponent {

    private val logger: LogHelper by inject()
    private val context = context.applicationContext
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleAlarm(showAtTime: LocalDateTime) {
        val triggerAtMillis = showAtTime
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()

        logger.v(TAG, "Scheduling AlarmManager alarm for $showAtTime ($triggerAtMillis)")
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            getAlarmPendingIntent()
        )
    }

    override fun cancelAlarm() {
        logger.v(TAG, "Cancelling AlarmManager alarm")
        alarmManager.cancel(getAlarmPendingIntent())
    }

    private fun getAlarmPendingIntent(): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SHOW_NOTIFICATION
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "AlarmScheduler"
        private const val REQUEST_CODE_ALARM = 1001
    }
}
