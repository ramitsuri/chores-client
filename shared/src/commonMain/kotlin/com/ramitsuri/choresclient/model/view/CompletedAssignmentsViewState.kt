package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.filter.Filter

data class CompletedAssignmentsViewState(
    val loading: Boolean = true,
    val assignments: List<TaskAssignmentDetails> = emptyList(),
    val filters: List<Filter> = emptyList()
)
