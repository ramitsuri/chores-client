package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.filter.Filter

data class AssignmentsViewState(
    val loading: Boolean = true,
    val assignments: Assignments = Assignments.default(),
    val filters: List<Filter> = listOf()
)

data class Assignments(
    val onCompletion: List<TaskAssignmentDetails>,
    val pastDue: List<TaskAssignmentDetails>,
    val dueToday: List<TaskAssignmentDetails>,
    val dueInFuture: List<TaskAssignmentDetails>,
    val otherAssignmentsCount: Map<String, Int>
) {
    fun isEmpty(): Boolean {
        return onCompletion.isEmpty() &&
                pastDue.isEmpty() &&
                dueToday.isEmpty() &&
                dueInFuture.isEmpty()

    }

    companion object {
        fun default(): Assignments {
            return Assignments(listOf(), listOf(), listOf(), listOf(), mapOf())
        }
    }
}