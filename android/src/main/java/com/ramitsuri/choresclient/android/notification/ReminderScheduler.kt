package com.ramitsuri.choresclient.android.notification

import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
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
            // Schedule only reminders from one week in the past. We don't want to be showing
            // reminders for assignments that are already way back in the past
            val now = ZonedDateTime.now()
            val sinceDueDateTime = now.minusWeeks(1).toInstant()
            val assignments = taskAssignmentsRepository.getSince(sinceDueDateTime)
            val memberId = prefManager.getUserId("")

            val completed = assignments.filter {
                it.progressStatus == ProgressStatus.DONE ||
                        it.progressStatus == ProgressStatus.UNKNOWN
            }
            completed.forEach { assignment ->
                alarmHandler.cancel(assignment)
            }

            val toSchedule = assignments.filter {
                it.member.id == memberId &&
                        it.dueDateTime > now.toInstant() &&
                        it.progressStatus == ProgressStatus.TODO &&
                        it.task.repeatUnit != RepeatUnit.ON_COMPLETE
            }
            toSchedule.forEach { assignment ->
                alarmHandler.schedule(assignment)
            }
        }
    }

    private fun log(message: String) {
        Timber.d(message)
    }
}