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

    suspend fun scheduleReminders(
        assignments: List<TaskAssignment>,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val memberId = prefManager.getLoggedInMemberId() ?: return
        val remindPastDue = prefManager.remindPastDue()
        val existingAlarms = alarmHandler.getExisting()

        val (inFuture, pastDue) = assignments
            .filter {
                it.memberId == memberId
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
    ): List<AssignmentAlarm> {
        val assignmentAlarms = mutableListOf<AssignmentAlarm>()

        inFuture.forEach { assignment ->
            if (assignment.repeatInfo.repeatUnit == RepeatUnit.ON_COMPLETE) {
                // Don't schedule reminder for on complete ones unless requested by user
                // (which will be taken care of in past due)
                return@forEach
            }
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
                if (lastNotifiedTime != null && now.minus(days = 1) < lastNotifiedTime) {
                    // It's been less than 1 day since the notification was last shown
                    return@forEach
                }
                assignmentAlarms.add(
                    AssignmentAlarm(
                        assignment.id,
                        showAtTime,
                    )
                )
            }
        }

        return assignmentAlarms
    }
}