package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.filter.Filter

data class TasksViewState(
    val loading: Boolean = true,
    val tasks: List<TaskDetails> = emptyList(),
    val filters: List<Filter> = emptyList()
)
