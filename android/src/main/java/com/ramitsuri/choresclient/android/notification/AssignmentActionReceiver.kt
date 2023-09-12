package com.ramitsuri.choresclient.android.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.android.utils.NotificationAction
import com.ramitsuri.choresclient.android.utils.NotificationActionExtra
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AssignmentActionReceiver : BroadcastReceiver(), KoinComponent {
    private val coroutineScope: CoroutineScope by inject()
    private val repository: TaskAssignmentsRepository by inject()
    private val logger: LogHelper by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val assignmentId = intent.getStringExtra(NotificationActionExtra.KEY_ASSIGNMENT_ID) ?: ""
        when (action) {
            NotificationAction.SNOOZE_HOUR.action -> {
                coroutineScope.launch {
                    repository.onSnoozeHourRequested(assignmentId)
                }
            }

            NotificationAction.SNOOZE_DAY.action -> {
                coroutineScope.launch {
                    repository.onSnoozeDayRequested(assignmentId)
                }
            }

            NotificationAction.COMPLETE.action -> {
                logger.v(TAG, "Marked $assignmentId as done")
                coroutineScope.launch {
                    repository.markTaskAssignmentDone(assignmentId, Clock.System.now())
                }
            }

            NotificationAction.WONT_DO.action -> {
                logger.v(TAG, "Marked $assignmentId as won't do")
                coroutineScope.launch {
                    repository.markTaskAssignmentWontDo(assignmentId, Clock.System.now())
                }
            }

            else -> {
                // Do nothing
            }
        }
    }

    companion object {
        private const val TAG = "AssignmentActionReceiver"
    }
}