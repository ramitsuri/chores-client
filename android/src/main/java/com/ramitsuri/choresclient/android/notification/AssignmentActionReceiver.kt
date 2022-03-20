package com.ramitsuri.choresclient.android.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.data.AssignmentAlarm
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.reminder.AlarmHandler
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.Base
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.NotificationAction
import com.ramitsuri.choresclient.android.utils.NotificationActionExtra
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AssignmentActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var repo: TaskAssignmentsRepository

    @Inject
    lateinit var alarmHandler: AlarmHandler

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val assignmentId = intent.getStringExtra(NotificationActionExtra.KEY_ASSIGNMENT_ID) ?: ""
        val notificationId = intent.getIntExtra(NotificationActionExtra.KEY_NOTIFICATION_ID, -1)
        val notificationText = intent.getStringExtra(NotificationActionExtra.KEY_NOTIFICATION_TEXT)
            ?: requireNotNull(context).getString(R.string.notification_reminder_message)
        when (action) {
            NotificationAction.SNOOZE_HOUR.action -> {
                onSnoozeHourRequested(assignmentId, notificationId, notificationText)
                notificationHandler.cancelNotification(notificationId)
            }
            NotificationAction.SNOOZE_DAY.action -> {
                onSnoozeDayRequested(assignmentId, notificationId, notificationText)
                notificationHandler.cancelNotification(notificationId)
            }
            NotificationAction.COMPLETE.action -> {
                onCompleteRequested(assignmentId)
                notificationHandler.cancelNotification(notificationId)
            }
            else -> {
                // Do nothing
            }
        }
    }

    private fun onSnoozeHourRequested(
        assignmentId: String,
        notificationId: Int,
        notificationText: String
    ) {
        Timber.i("Action snooze hour requested for $assignmentId, $notificationId")
        val snoozeBySeconds = Base.SNOOZE_HOUR
        snooze(assignmentId, notificationId, notificationText, snoozeBySeconds)
    }

    private fun onSnoozeDayRequested(
        assignmentId: String,
        notificationId: Int,
        notificationText: String
    ) {
        Timber.i("Action snooze day requested for $assignmentId, $notificationId")
        val snoozeBySeconds = Base.SNOOZE_DAY
        snooze(assignmentId, notificationId, notificationText, snoozeBySeconds)
    }

    private fun snooze(
        assignmentId: String,
        notificationId: Int,
        notificationText: String,
        snoozeBySeconds: Long
    ) {
        val assignmentAlarm = AssignmentAlarm(
            assignmentId,
            showAtTime = Instant.now().plusSeconds(snoozeBySeconds),
            notificationId,
            notificationText
        )
        coroutineScope.launch(dispatcherProvider.io) {
            alarmHandler.schedule(listOf(assignmentAlarm))
        }
    }

    private fun onCompleteRequested(assignmentId: String) {
        Timber.i("Action complete requested for $assignmentId")
        coroutineScope.launch(dispatcherProvider.io) {
            val assignment = repo.getLocal(assignmentId)
            if (assignment != null) {
                val update = assignment.copy(
                    progressStatus = ProgressStatus.DONE,
                    progressStatusDate = Instant.now()
                )
                repo.updateTaskAssignment(update, true)
            }
        }
    }
}