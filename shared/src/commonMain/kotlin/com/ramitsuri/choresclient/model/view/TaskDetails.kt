package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.entities.Task

data class TaskDetails(
    val task: Task,
    val memberName: String,
    val houseName: String,
    val isSharedList: Boolean = false
)
