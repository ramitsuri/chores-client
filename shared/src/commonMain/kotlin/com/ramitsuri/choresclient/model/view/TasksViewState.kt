package com.ramitsuri.choresclient.model.view

data class TasksViewState(
    val loading: Boolean = true,
    val tasks: List<TaskDetails> = emptyList(),
    val selectedListTypes: Set<ListTypeSelection> = emptySet(),
    val searchQuery: String = "",
)
