package com.ramitsuri.choresclient.model.view

data class TasksViewState(
    val loading: Boolean = true,
    val tasks: List<TaskDetails> = emptyList(),
    val selectedListTypes: Set<ListTypeSelection> = emptySet(),
    val searchQuery: String = "",
    val unsyncedTaskCount: Int = 0,
    val isUploading: Boolean = false,
    val isSyncScheduled: Boolean = false,
)
