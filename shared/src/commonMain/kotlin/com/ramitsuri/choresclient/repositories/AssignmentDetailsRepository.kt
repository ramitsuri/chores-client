package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.model.AssignmentDetails
import com.ramitsuri.choresclient.notification.NotificationHandler
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.getNewReminderTimeSnoozeDay
import com.ramitsuri.choresclient.utils.getNewReminderTimeSnoozeHour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AssignmentDetailsRepository(
    private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val alarmHandler: AlarmHandler,
    private val notificationHandler: NotificationHandler
) : KoinComponent {
    private val logger: LogHelper by inject()

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
        logger.v(TAG, "Complete requested for $assignmentId")
        coroutineScope.launch(dispatcherProvider.io) {
            onCompleteRequestedSuspend(assignmentId)
        }
    }

    fun onWontDoRequested(assignmentId: String) {
        logger.v(TAG, "Won't do requested for $assignmentId")
        coroutineScope.launch(dispatcherProvider.io) {
            onWontDoRequestedSuspend(assignmentId)
        }
    }

    suspend fun getDetails(assignmentId: String): AssignmentDetails? {
        val assignment = taskAssignmentsRepository.getLocal(assignmentId)
        val alarmEntity = alarmHandler.getExisting(assignmentId)
        // Delay because there's choppy animation when switching from loading to showing details.
        // Temp fix for now
        delay(400)
        if (assignment == null) {
            return null
        }
        return AssignmentDetails(
            id = assignmentId,
            taskId = assignment.task.id,
            name = assignment.task.name,
            member = assignment.member.name,
            description = assignment.task.description,
            repeatValue = assignment.task.repeatValue,
            repeatUnit = assignment.task.repeatUnit,
            notificationTime = alarmEntity?.showAtTime
        )
    }

    suspend fun onCompleteRequestedSuspend(assignmentId: String) {
        logger.v(TAG, "Complete requested suspend for $assignmentId")
        cancelNotification(assignmentId)
        alarmHandler.cancel(listOf(assignmentId))
        taskAssignmentsRepository.markTaskAssignmentDone(assignmentId, Clock.System.now())
    }

    suspend fun onWontDoRequestedSuspend(assignmentId: String) {
        logger.v(TAG, "Won't do requested suspend for $assignmentId")
        cancelNotification(assignmentId)
        alarmHandler.cancel(listOf(assignmentId))
        taskAssignmentsRepository.markTaskAssignmentWontDo(assignmentId, Clock.System.now())
    }

    private suspend fun cancelNotification(assignmentId: String) {
        logger.v(TAG, "Cancel requested for $assignmentId")
        val existing = alarmHandler.getExisting(assignmentId)
        existing?.let {
            notificationHandler.cancelNotification(it.systemNotificationId.toInt())
        } ?: run {
            logger.v(TAG, "Existing $assignmentId not found, cannot cancel notification")
        }
    }

    companion object {
        private const val TAG = "AssignmentDetailsRepository"
    }
}