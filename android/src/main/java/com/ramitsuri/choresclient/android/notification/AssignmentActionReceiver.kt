package com.ramitsuri.choresclient.android.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.utils.NotificationAction
import com.ramitsuri.choresclient.android.utils.NotificationActionExtra
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AssignmentActionReceiver : BroadcastReceiver(), KoinComponent {
    private val detailsRepository: AssignmentDetailsRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val assignmentId = intent.getStringExtra(NotificationActionExtra.KEY_ASSIGNMENT_ID) ?: ""
        val notificationText = intent.getStringExtra(NotificationActionExtra.KEY_NOTIFICATION_TEXT)
            ?: requireNotNull(context).getString(R.string.notification_reminder_title)
        when (action) {
            NotificationAction.SNOOZE_HOUR.action -> {
                detailsRepository.onSnoozeHourRequested(
                    assignmentId,
                    notificationText
                )
            }
            NotificationAction.SNOOZE_DAY.action -> {
                detailsRepository.onSnoozeDayRequested(
                    assignmentId,
                    notificationText
                )
            }
            NotificationAction.COMPLETE.action -> {
                detailsRepository.onCompleteRequested(assignmentId)
            }
            else -> {
                // Do nothing
            }
        }
    }
}