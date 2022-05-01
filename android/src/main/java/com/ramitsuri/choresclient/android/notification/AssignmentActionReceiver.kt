package com.ramitsuri.choresclient.android.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.repositories.AssignmentActionManager
import com.ramitsuri.choresclient.android.utils.NotificationAction
import com.ramitsuri.choresclient.android.utils.NotificationActionExtra
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AssignmentActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var assignmentActionManager: AssignmentActionManager

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val assignmentId = intent.getStringExtra(NotificationActionExtra.KEY_ASSIGNMENT_ID) ?: ""
        val notificationText = intent.getStringExtra(NotificationActionExtra.KEY_NOTIFICATION_TEXT)
            ?: requireNotNull(context).getString(R.string.notification_reminder_title)
        when (action) {
            NotificationAction.SNOOZE_HOUR.action -> {
                assignmentActionManager.onSnoozeHourRequested(
                    assignmentId,
                    notificationText
                )
            }
            NotificationAction.SNOOZE_DAY.action -> {
                assignmentActionManager.onSnoozeDayRequested(
                    assignmentId,
                    notificationText
                )
            }
            NotificationAction.COMPLETE.action -> {
                assignmentActionManager.onCompleteRequested(assignmentId)
            }
            else -> {
                // Do nothing
            }
        }
    }
}