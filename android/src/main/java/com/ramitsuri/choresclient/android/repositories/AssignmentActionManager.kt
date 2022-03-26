package com.ramitsuri.choresclient.android.repositories

import com.ramitsuri.choresclient.android.reminder.AlarmHandler
import com.ramitsuri.choresclient.android.utils.Base
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class AssignmentActionManager @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val alarmHandler: AlarmHandler
) {

    fun onSnoozeHourRequested(
        assignmentId: String,
        notificationText: String
    ) {
        Timber.i("Action snooze hour requested for $assignmentId")
        val snoozeBySeconds = Base.SNOOZE_HOUR
        snooze(assignmentId, notificationText, snoozeBySeconds)
    }

    fun onSnoozeDayRequested(
        assignmentId: String,
        notificationText: String
    ) {
        Timber.i("Action snooze day requested for $assignmentId")
        val snoozeBySeconds = Base.SNOOZE_DAY
        snooze(assignmentId, notificationText, snoozeBySeconds)
    }

    fun onCompleteRequested(assignmentId: String) {
        coroutineScope.launch(dispatcherProvider.io) {
            onCompleteRequestedSuspend(assignmentId)
        }
    }

    suspend fun onCompleteRequestedSuspend(assignmentId: String) {
        Timber.i("Action complete requested for $assignmentId")
        alarmHandler.cancel(listOf(assignmentId))
        taskAssignmentsRepository.markTaskAssignmentDone(assignmentId, Instant.now())
    }

    private fun snooze(
        assignmentId: String,
        notificationText: String,
        snoozeBySeconds: Long
    ) {
        val showAtTime = Instant.now().plusSeconds(snoozeBySeconds)
        coroutineScope.launch(dispatcherProvider.io) {
            alarmHandler.reschedule(assignmentId, showAtTime, notificationText)
        }
    }
}