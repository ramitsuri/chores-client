package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.entities.TaskAssignment
import kotlinx.datetime.LocalDateTime

data class TaskAssignmentDetails(
    val taskAssignment: TaskAssignment,
    val reminderTime: LocalDateTime?,
    val enableSnooze: Boolean,
    val willReminderBeSet: Boolean,
    val assignedToLoggedInUser: Boolean,
    val isSharedList: Boolean = false,
    val isTaskChangeSyncPending: Boolean = false,
)

fun List<TaskAssignmentDetails>.filterByListTypes(selectedListTypes: Set<ListTypeSelection>): List<TaskAssignmentDetails> {
    if (selectedListTypes.size == ListTypeSelection.entries.size) {
        return this
    }
    val isShared = selectedListTypes.contains(ListTypeSelection.SHARED)
    return filter { it.isSharedList == isShared }
}
