package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.TaskAssignment

data class TaskAssignmentWrapper(
    val assignment: TaskAssignment,
    val enableCompleteButton: Boolean
)