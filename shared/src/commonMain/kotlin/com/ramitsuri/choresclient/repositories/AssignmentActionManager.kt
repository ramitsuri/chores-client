package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.Base
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class AssignmentActionManager(
    private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val alarmHandler: AlarmHandler
) {

    fun onSnoozeHourRequested(
        assignmentId: String,
        notificationText: String
    ) {
        val snoozeBySeconds = Base.SNOOZE_HOUR
        snooze(assignmentId, notificationText, snoozeBySeconds)
    }

    fun onSnoozeDayRequested(
        assignmentId: String,
        notificationText: String
    ) {
        val snoozeBySeconds = Base.SNOOZE_DAY
        snooze(assignmentId, notificationText, snoozeBySeconds)
    }

    fun onCompleteRequested(assignmentId: String) {
        coroutineScope.launch(dispatcherProvider.io) {
            onCompleteRequestedSuspend(assignmentId)
        }
    }

    suspend fun onCompleteRequestedSuspend(assignmentId: String) {
        alarmHandler.cancel(listOf(assignmentId))
        taskAssignmentsRepository.markTaskAssignmentDone(assignmentId, Clock.System.now())
    }

    private fun snooze(
        assignmentId: String,
        notificationText: String,
        snoozeBySeconds: Long
    ) {
        val newTime = Clock.System.now().toEpochMilliseconds() + snoozeBySeconds * 1000
        val showAtTime = Instant.fromEpochMilliseconds(newTime)
        coroutineScope.launch(dispatcherProvider.io) {
            alarmHandler.reschedule(assignmentId, showAtTime, notificationText)
        }
    }
}