package com.ramitsuri.choresclient.model.view

import kotlinx.datetime.TimeZone

data class CompletedAssignmentsViewState(
    val loading: Boolean = true,
    val assignments: List<TaskAssignmentDetails> = emptyList(),
    val selectedListTypes: Set<ListTypeSelection> = emptySet(),
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
)
