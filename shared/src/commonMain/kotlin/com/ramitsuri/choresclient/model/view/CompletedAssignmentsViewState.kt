package com.ramitsuri.choresclient.model.view

data class CompletedAssignmentsViewState(
    val loading: Boolean = true,
    val assignments: List<TaskAssignmentDetails> = emptyList(),
)
