package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.entities.TaskAssignment
import kotlinx.datetime.LocalDateTime

data class TaskAssignmentDetails(
    val taskAssignment: TaskAssignment,
    val reminderTime: LocalDateTime?,
    val enableSnooze: Boolean,
    val willReminderBeSet: Boolean,
    val assignedToLoggedInUser: Boolean,
)