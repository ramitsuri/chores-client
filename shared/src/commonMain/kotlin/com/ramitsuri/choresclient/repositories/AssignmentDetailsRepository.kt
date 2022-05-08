package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.model.AssignmentDetails
import com.ramitsuri.choresclient.notification.NotificationHandler
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.getNewReminderTimeSnoozeDay
import com.ramitsuri.choresclient.utils.getNewReminderTimeSnoozeHour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AssignmentDetailsRepository(
    private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val alarmHandler: AlarmHandler,
    private val notificationHandler: NotificationHandler
) {

    fun onSnoozeHourRequested(
        assignmentId: String,
        notificationText: String
    ) {
        val showAtTime = getNewReminderTimeSnoozeHour()
        coroutineScope.launch(dispatcherProvider.io) {
            cancelNotification(assignmentId)
            alarmHandler.reschedule(assignmentId, showAtTime, notificationText)
        }
    }

    fun onSnoozeDayRequested(
        assignmentId: String,
        notificationText: String
    ) {
        val showAtTime = getNewReminderTimeSnoozeDay()
        coroutineScope.launch(dispatcherProvider.io) {
            cancelNotification(assignmentId)
            alarmHandler.reschedule(assignmentId, showAtTime, notificationText)
        }
    }

    fun onCompleteRequested(assignmentId: String) {
        coroutineScope.launch(dispatcherProvider.io) {
            onCompleteRequestedSuspend(assignmentId)
        }
    }

    suspend fun getDetails(assignmentId: String): AssignmentDetails? {
        val assignment = taskAssignmentsRepository.getLocal(assignmentId)
        val alarmEntity = alarmHandler.getExisting(assignmentId)
        if (assignment == null) {
            return null
        }
        return AssignmentDetails(
            id = assignmentId,
            name = assignment.task.name,
            description = assignment.task.description,
            repeatValue = assignment.task.repeatValue,
            repeatUnit = assignment.task.repeatUnit,
            notificationTime = alarmEntity?.showAtTime
        )
    }

    suspend fun onCompleteRequestedSuspend(assignmentId: String) {
        cancelNotification(assignmentId)
        alarmHandler.cancel(listOf(assignmentId))
        taskAssignmentsRepository.markTaskAssignmentDone(assignmentId, Clock.System.now())
    }

    private suspend fun cancelNotification(assignmentId: String) {
        val existing = alarmHandler.getExisting(assignmentId)
        existing?.let {
            notificationHandler.cancelNotification(it.systemNotificationId.toInt())
        }
    }
}