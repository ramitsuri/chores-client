package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.filter.Filter

data class AssignmentsViewState(
    val loading: Boolean = true,
    val assignments: Assignments = Assignments.default(),
    val filters: List<Filter> = listOf()
)

sealed class Assignments {
    data class OldStyle(
        val groups: Map<TextValue, List<TaskAssignmentDetails>>
    ) : Assignments()

    data class NewStyle(
        val onCompletion: List<TaskAssignmentDetails>,
        val pastDue: List<TaskAssignmentDetails>,
        val dueToday: List<TaskAssignmentDetails>,
        val dueInFuture: List<TaskAssignmentDetails>,
        val otherAssignmentsCount: Map<String, Int>
    ) : Assignments()

    fun isEmpty(): Boolean {
        return when (this) {
            is NewStyle -> {
                onCompletion.isEmpty() &&
                        pastDue.isEmpty() &&
                        dueToday.isEmpty() &&
                        dueInFuture.isEmpty()
            }

            is OldStyle -> {
                groups.isEmpty()
            }
        }
    }

    companion object {
        fun default(): Assignments {
            return NewStyle(listOf(), listOf(), listOf(), listOf(), mapOf())
        }
    }
}