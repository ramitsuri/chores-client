package com.ramitsuri.choresclient.android.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ramitsuri.choresclient.android.MainActivity

class SystemNotificationHandler(context: Context) : NotificationHandler {
    private val context = context.applicationContext
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    override fun createChannels(channels: List<NotificationChannelInfo>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        for (channelInfo in channels) {
            notificationManager?.createNotificationChannel(channelInfo.toPlatformChannel())
        }
    }

    override fun buildAndShow(notificationInfo: NotificationInfo) {
        val builder = NotificationCompat.Builder(context, notificationInfo.channelId)
        builder.apply {
            priority = notificationInfo.priority.toPlatformValue()
            setSmallIcon(notificationInfo.iconResId)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(context.getString(notificationInfo.titleResId))
            setContentText(notificationInfo.body)
            if (notificationInfo.actions != null) {
                for (action in notificationInfo.actions) {
                    addAction(getAction(action))
                }
            }
            setAutoCancel(true)
            setContentIntent(getTapPendingIntent(MainActivity::class.java))
        }
        val notification = builder.build()
        notificationManager?.notify(notificationInfo.id, notification)
    }

    override fun cancelNotification(notificationId: Int) {
        notificationManager?.cancel(notificationId)
    }

    private fun getAction(actionInfo: NotificationActionInfo): NotificationCompat.Action {
        val intent = Intent(context, actionInfo.intentReceiverClass)
        intent.action = actionInfo.action
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            actionInfo.requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Action.Builder(
            0,
            context.getString(actionInfo.textResId),
            pendingIntent
        ).build()
    }

    private fun getTapPendingIntent(intentReceiverClass: Class<*>): PendingIntent {
        val intent = Intent(context, intentReceiverClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}