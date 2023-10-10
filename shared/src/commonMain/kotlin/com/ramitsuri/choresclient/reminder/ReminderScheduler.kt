package com.ramitsuri.choresclient.reminder

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.model.entities.AssignmentAlarm
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.utils.minus
import com.ramitsuri.choresclient.utils.now
import com.ramitsuri.choresclient.utils.plus
import kotlinx.datetime.LocalDateTime

class ReminderScheduler(
    private val alarmHandler: AlarmHandler,
    private val prefManager: PrefManager
) {

    /**
     * Schedules reminders for passed in assignments but filters out assignments that are not for
     * the logged in member or have repeat unit as on_completion.
     */
    suspend fun scheduleReminders(
        assignments: List<TaskAssignment>,
        forceRemindPastDue: Boolean = false,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val memberId = prefManager.getLoggedInMemberId() ?: return
        val remindPastDue = prefManager.remindPastDue() || forceRemindPastDue
        val existingAlarms = alarmHandler.getExisting()

        val (inFuture, pastDue) = assignments
            .filter {
                it.memberId == memberId &&
                        it.repeatInfo.repeatUnit != RepeatUnit.ON_COMPLETE
            }
            .partition {
                it.dueDateTime > now
            }

        val alarmsToSchedule = getAlarmsToSchedule(
            inFuture = inFuture,
            pastDue = pastDue,
            existingAlarms = existingAlarms,
            now = now,
            remindPastDue = remindPastDue,
            forceRemindPastDue = forceRemindPastDue,
        )
        alarmHandler.schedule(alarmsToSchedule)

        val assignmentsEligibleForAlarm = if (remindPastDue) {
            inFuture.plus(pastDue)
        } else {
            inFuture
        }
            .map { it.id }
            .toSet()
        val alarmsToCancel = existingAlarms
            .map { it.assignmentId }
            .minus(assignmentsEligibleForAlarm)
        alarmHandler.cancel(alarmsToCancel)
    }

    private fun getAlarmsToSchedule(
        inFuture: List<TaskAssignment>,
        pastDue: List<TaskAssignment>,
        existingAlarms: List<AlarmEntity>,
        now: LocalDateTime,
        remindPastDue: Boolean,
        forceRemindPastDue: Boolean,
    ): List<AssignmentAlarm> {
        val assignmentAlarms = mutableListOf<AssignmentAlarm>()

        inFuture.forEach { assignment ->
            if (existingAlarms.find { assignment.id == it.assignmentId } != null) {
                return@forEach
            }
            assignmentAlarms.add(
                AssignmentAlarm(
                    assignment.id,
                    assignment.dueDateTime,
                )
            )
        }

        if (remindPastDue) {
            val showAtTime = now.plus(seconds = 60)
            pastDue.forEach { assignment ->
                val existingNotification = existingAlarms.find { notificationEntity ->
                    assignment.id == notificationEntity.assignmentId
                }
                val lastNotifiedTime = existingNotification?.showAtTime
                if (lastNotifiedTime.lessThanOneDaySinceLastNotified(now) && !forceRemindPastDue) {
                    // It's been less than 1 day since the notification was last shown and
                    // reminder for past due not forced
                    return@forEach
                }
                assignmentAlarms.add(
                    AssignmentAlarm(
                        assignment.id,
                        showAtTime,
                        existingNotification?.systemNotificationId?.toInt() ?: -1,
                    )
                )
            }
        }

        return assignmentAlarms
    }

    private fun LocalDateTime?.lessThanOneDaySinceLastNotified(now: LocalDateTime): Boolean {
        if (this == null) {
            return false
        }
        return now.minus(days = 1) < this
    }
}