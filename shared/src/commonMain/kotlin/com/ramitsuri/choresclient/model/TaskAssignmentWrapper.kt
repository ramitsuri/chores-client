package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.TaskAssignment

data class TaskAssignmentWrapper(
    val headerView: String? = null,
    val itemView: TaskAssignment? = null
)