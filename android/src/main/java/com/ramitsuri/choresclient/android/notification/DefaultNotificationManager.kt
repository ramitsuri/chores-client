package com.ramitsuri.choresclient.android.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ramitsuri.choresclient.android.main.MainActivity
import com.ramitsuri.choresclient.notification.Importance
import com.ramitsuri.choresclient.notification.NotificationActionInfo
import com.ramitsuri.choresclient.notification.NotificationChannelInfo
import com.ramitsuri.choresclient.notification.NotificationInfo
import com.ramitsuri.choresclient.notification.NotificationManager
import com.ramitsuri.choresclient.utils.LogHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultNotificationManager(context: Context) : NotificationManager, KoinComponent {
    private val logger: LogHelper by inject()
    private val context = context.applicationContext
    private val notificationManager = NotificationManagerCompat.from(context)

    override fun createChannels(channels: List<NotificationChannelInfo>) {
        for (channelInfo in channels) {
            notificationManager.createNotificationChannel(toPlatformChannel(channelInfo))
        }
    }

    override fun showNotification(notificationInfo: NotificationInfo) {
        val builder = NotificationCompat.Builder(context, notificationInfo.channelId)
        builder.apply {
            priority = notificationInfo.priority.toPlatformValue()
            setSmallIcon(notificationInfo.iconResId)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(notificationInfo.title)
            if (!notificationInfo.body.isNullOrEmpty()) {
                setContentText(notificationInfo.body)
            }
            if (!notificationInfo.additionalText.isNullOrEmpty()) {
                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(notificationInfo.additionalText)
                )
            }
            notificationInfo.actions?.let {
                for ((index, action) in it.withIndex()) {
                    addAction(
                        getAction(
                            notificationInfo.id,
                            index,
                            action,
                            notificationInfo.actionExtras
                        )
                    )
                }
            }
            setAutoCancel(true)
            setContentIntent(getTapPendingIntent(MainActivity::class.java))
        }
        val notification = builder.build()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationInfo.id, notification)
    }

    override fun cancelNotification(notificationId: Int) {
        logger.v(TAG, "Cancel notification requested for $notificationId")
        notificationManager.cancel(notificationId)
    }

    private fun getAction(
        notificationId: Int,
        actionIndex: Int,
        actionInfo: NotificationActionInfo,
        actionExtras: Map<String, Any>?
    ): NotificationCompat.Action {
        val actionRequestCode = notificationId * 10 + actionIndex
        val intent = Intent(context, actionInfo.intentReceiverClass.java)
        intent.action = actionInfo.action
        actionExtras?.forEach { (extraKey, extraValue) ->
            when (extraValue) {
                is String -> {
                    intent.putExtra(extraKey, extraValue)
                }

                is Int -> {
                    intent.putExtra(extraKey, extraValue)
                }

                is Long -> {
                    intent.putExtra(extraKey, extraValue)
                }

                is Boolean -> {
                    intent.putExtra(extraKey, extraValue)
                }
            }
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            actionRequestCode,
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

    private fun toPlatformChannel(channelInfo: NotificationChannelInfo): NotificationChannel {
        val importance = toPlatformImportance(channelInfo.importance)
        val channel = NotificationChannel(channelInfo.id, channelInfo.name, importance).apply {
            description = description
            vibrationPattern = null
            enableVibration(
                !(channelInfo.importance == Importance.MIN ||
                        channelInfo.importance == Importance.NONE)
            )

        }
        return channel
    }

    // Returns the platformValue for now but could change with future Android versions
    private fun toPlatformImportance(importance: Importance): Int {
        return when (importance) {
            Importance.NONE -> android.app.NotificationManager.IMPORTANCE_NONE
            Importance.MIN -> android.app.NotificationManager.IMPORTANCE_MIN
            Importance.LOW -> android.app.NotificationManager.IMPORTANCE_LOW
            Importance.DEFAULT -> android.app.NotificationManager.IMPORTANCE_DEFAULT
            Importance.HIGH -> android.app.NotificationManager.IMPORTANCE_HIGH
            Importance.MAX -> android.app.NotificationManager.IMPORTANCE_MAX
        }
    }

    companion object {
        private const val TAG = "NotificationManager"
    }
}