package com.ramitsuri.choresclient.android.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.repositories.AssignmentActionManager
import com.ramitsuri.choresclient.android.utils.NotificationAction
import com.ramitsuri.choresclient.android.utils.NotificationActionExtra
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AssignmentActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var assignmentActionManager: AssignmentActionManager

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val assignmentId = intent.getStringExtra(NotificationActionExtra.KEY_ASSIGNMENT_ID) ?: ""
        val notificationId = intent.getIntExtra(NotificationActionExtra.KEY_NOTIFICATION_ID, -1)
        val notificationText = intent.getStringExtra(NotificationActionExtra.KEY_NOTIFICATION_TEXT)
            ?: requireNotNull(context).getString(R.string.notification_reminder_title)
        when (action) {
            NotificationAction.SNOOZE_HOUR.action -> {
                assignmentActionManager.onSnoozeHourRequested(
                    assignmentId,
                    notificationText
                )
                notificationHandler.cancelNotification(notificationId)
            }
            NotificationAction.SNOOZE_DAY.action -> {
                assignmentActionManager.onSnoozeDayRequested(
                    assignmentId,
                    notificationText
                )
                notificationHandler.cancelNotification(notificationId)
            }
            NotificationAction.COMPLETE.action -> {
                assignmentActionManager.onCompleteRequested(assignmentId)
                notificationHandler.cancelNotification(notificationId)
            }
            else -> {
                // Do nothing
            }
        }
    }
}