package com.ramitsuri.choresclient.android.notification

import com.ramitsuri.choresclient.android.data.ReminderAssignmentDao
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.reminder.AlarmHandler
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.ZonedDateTime

class ReminderScheduler(
    private val reminderAssignmentDao: ReminderAssignmentDao,
    private val alarmHandler: AlarmHandler,
    private val dispatchers: DispatcherProvider,
    private val dueDateTimeToReminderTimeConverter: (Instant) -> Instant
) {
    private var running = false
    private val assignments = mutableListOf<TaskAssignment>()

    suspend fun addReminders(taskAssignments: List<TaskAssignment>) {
        log("Add Reminders, $taskAssignments")
        if (running) {
            log("Already running")
            return
        }
        assignments.addAll(taskAssignments)
        running = true
        check()
        scheduleReminders()
        running = false
    }

    private suspend fun check() {
        log("Checking")
        withContext(dispatchers.default) {
            val iterator = assignments.iterator()
            while (iterator.hasNext()) {
                val assignment = iterator.next()
                val newTime =
                    dueDateTimeToReminderTimeConverter(assignment.dueDateTime).toEpochMilli()

                val existingReminderAssignment = reminderAssignmentDao.get(assignment.id)

                if (existingReminderAssignment == null) {
                    log("Existing null, adding new")
                    reminderAssignmentDao.insert(assignment.id, newTime)
                } else {
                    log("Exists")
                    val oldTime = existingReminderAssignment.time
                    if (oldTime != newTime) {
                        log("Time has changed")
                        val updateResult = reminderAssignmentDao.updateOrInsert(
                            assignmentId = assignment.id,
                            newTime = newTime,
                            oldTime = oldTime
                        )
                        if (updateResult.oldTimeNoLongerExists) {
                            log("Old time no longer exists")
                            alarmHandler.cancel(existingReminderAssignment.requestCode)
                        }
                    }
                }
                iterator.remove()
            }
        }
        log("Done")
    }

    private suspend fun scheduleReminders() {
        // Schedule only reminders from one hour in the past. We don't want to be showing reminders
        // for assignments that are already way back in the past
        val now = ZonedDateTime.now().minusHours(1).toInstant().toEpochMilli()
        withContext(dispatchers.io) {
            val requestCodeTimeAssociations = reminderAssignmentDao.getRequestCodeTimeAssociations()
            for (requestCodeTimeAssociation in requestCodeTimeAssociations) {
                if (requestCodeTimeAssociation.time < now) {
                    continue
                }
                alarmHandler.schedule(
                    requestCodeTimeAssociation.requestCode,
                    requestCodeTimeAssociation.time
                )
            }
        }
    }

    private fun log(message: String) {
        Timber.d(message)
    }
}