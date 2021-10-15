package com.ramitsuri.choresclient.android.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.notification.NotificationHandler
import com.ramitsuri.choresclient.android.notification.NotificationInfo
import com.ramitsuri.choresclient.android.notification.Priority
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver: BroadcastReceiver() {
    private val TAG = "ReminderReceiver"

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Received reminder intent")

        if (context != null) {
            notificationHandler.buildAndShow(
                NotificationInfo(
                    1,
                    context.getString(R.string.notification_reminders_id),
                    Priority.HIGH,
                    R.string.notification_reminder_title,
                    R.string.notification_reminder_message,
                    R.drawable.ic_notification
                )
            )
        }
    }
}