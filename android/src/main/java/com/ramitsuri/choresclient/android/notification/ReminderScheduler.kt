package com.ramitsuri.choresclient.android.notification

import com.ramitsuri.choresclient.android.data.AlarmEntity
import com.ramitsuri.choresclient.android.data.AssignmentAlarm
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.reminder.AlarmHandler
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.ZonedDateTime

class ReminderScheduler(
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val alarmHandler: AlarmHandler,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
) {
    private var running = false

    suspend fun addReminders() {
        log("Add Reminders")
        if (running) {
            log("Already running")
            return
        }
        running = true
        scheduleReminders()
        running = false
    }

    private suspend fun scheduleReminders() {
        withContext(dispatchers.io) {
            // Schedule only reminders from one week in the past. We don't want to be handling
            // assignments to far back in the past as they are assumed to be handled by now
            val now = ZonedDateTime.now()
            val sinceDueDateTime = now.minusWeeks(3).toInstant()
            val assignments = taskAssignmentsRepository.getLocal(sinceDueDateTime)
            val memberId = prefManager.getUserId() ?: ""
            val existingNotifications = alarmHandler.getExisting()

            val handledIds = mutableListOf<String>().apply {
                addAll(handleCompleted(assignments))
                addAll(handlePastDue(assignments, existingNotifications, now, memberId))
                addAll(handleToSchedule(assignments, existingNotifications, now, memberId))
            }

            val originalIds = assignments.map { it.id }
            val unhandledIds = originalIds.minus(handledIds)
            alarmHandler.cancel(unhandledIds)
        }
    }

    private suspend fun handleCompleted(assignments: List<TaskAssignment>): List<String> {
        val completed = assignments.filter {
            it.progressStatus == ProgressStatus.DONE ||
                    it.progressStatus == ProgressStatus.UNKNOWN
        }
        alarmHandler.cancel(completed.map { it.id })
        return completed.map { it.id }
    }

    private suspend fun handlePastDue(
        assignments: List<TaskAssignment>,
        existingAlarms: List<AlarmEntity>,
        now: ZonedDateTime,
        memberId: String
    ): List<String> {
        val newAssignmentAlarms = mutableListOf<AssignmentAlarm>()
        val scheduledTime = now.toInstant().plusSeconds(60)
        val missed = assignments.filter {
            it.member.id == memberId &&
                    it.dueDateTime <= now.toInstant() &&
                    it.progressStatus == ProgressStatus.TODO &&
                    it.task.repeatUnit != RepeatUnit.ON_COMPLETE
        }
        missed.forEach { assignment ->
            val existingNotification = existingAlarms.find { notificationEntity ->
                assignment.id == notificationEntity.assignmentId
            }
            val newNotificationId: Int = if (existingNotification != null) {
                // It's been more than 1 day since the notification was last shown
                if (now.minusDays(1).toInstant() > existingNotification.showAtTime) {
                    existingNotification.systemNotificationId
                } else {
                    return@forEach
                }
            } else {
                prefManager.generateNewNotificationId()
            }
            newAssignmentAlarms.add(
                AssignmentAlarm(
                    assignment.id,
                    scheduledTime,
                    newNotificationId,
                    assignment.task.name
                )
            )
        }
        alarmHandler.schedule(newAssignmentAlarms)
        return missed.map { it.id }
    }

    private suspend fun handleToSchedule(
        assignments: List<TaskAssignment>,
        existingAlarms: List<AlarmEntity>,
        now: ZonedDateTime,
        memberId: String
    ): List<String> {
        // Find assignments to schedule
        val newAssignmentAlarms = mutableListOf<AssignmentAlarm>()
        val inFuture = assignments.filter {
            it.member.id == memberId &&
                    it.dueDateTime > now.toInstant() &&
                    it.progressStatus == ProgressStatus.TODO &&
                    it.task.repeatUnit != RepeatUnit.ON_COMPLETE
        }
        inFuture.forEach { assignment ->
            // Skip the assignments that already have a notification
            if (existingAlarms.find { assignment.id == it.assignmentId } != null) {
                return@forEach
            }
            // Schedule if there isn't a notification already
            val notificationId = prefManager.generateNewNotificationId()
            newAssignmentAlarms.add(
                AssignmentAlarm(
                    assignment.id,
                    assignment.dueDateTime,
                    notificationId,
                    assignment.task.name
                )
            )
        }
        alarmHandler.schedule(newAssignmentAlarms)
        return inFuture.map { it.id }
    }

    private fun log(message: String) {
        Timber.d(message)
    }
}