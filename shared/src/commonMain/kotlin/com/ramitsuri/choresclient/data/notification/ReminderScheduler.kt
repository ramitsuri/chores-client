package com.ramitsuri.choresclient.data.notification

import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.data.entities.AssignmentAlarm
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.Lock
import com.ramitsuri.choresclient.utils.use
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class ReminderScheduler(
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val alarmHandler: AlarmHandler,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
) {
    private var running = false
    private val runningLock = Lock()

    suspend fun addReminders() {
        runningLock.use {
            if (running) {
                return
            }
        }

        runningLock.use {
            running = true
        }
        scheduleReminders(Clock.System.now())
        runningLock.use {
            running = false
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun scheduleReminders(now: Instant) {
        withContext(dispatchers.io) {
            // Schedule only reminders from one week in the past. We don't want to be handling
            // assignments to far back in the past as they are assumed to be handled by now
            val previousDuration = 21.days
            val sinceDueDateTime = now.minus(previousDuration)
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

    @OptIn(ExperimentalTime::class)
    private suspend fun handlePastDue(
        assignments: List<TaskAssignment>,
        existingAlarms: List<AlarmEntity>,
        now: Instant,
        memberId: String
    ): List<String> {
        val newAssignmentAlarms = mutableListOf<AssignmentAlarm>()
        val duration = 60.seconds
        val scheduledTime = now.plus(duration)
        val missed = assignments.filter {
            it.member.id == memberId &&
                    it.dueDateTime <= now &&
                    it.progressStatus == ProgressStatus.TODO &&
                    it.task.repeatUnit != RepeatUnit.ON_COMPLETE
        }
        missed.forEach { assignment ->
            val existingNotification = existingAlarms.find { notificationEntity ->
                assignment.id == notificationEntity.assignmentId
            }
            val newNotificationId: Int = if (existingNotification != null) {
                // It's been more than 1 day since the notification was last shown
                if (now.minus(1.days) > existingNotification.showAtTime) {
                    existingNotification.systemNotificationId.toInt()
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
        now: Instant,
        memberId: String
    ): List<String> {
        // Find assignments to schedule
        val newAssignmentAlarms = mutableListOf<AssignmentAlarm>()
        val inFuture = assignments.filter {
            it.member.id == memberId &&
                    it.dueDateTime > now &&
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
}