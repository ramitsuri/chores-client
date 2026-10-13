package com.ramitsuri.choresclient.model.view

data class AssignmentsViewState(
    val loading: Boolean = true,
    val assignments: Assignments = Assignments.default(),
    val customSnoozeHours: String = "",
    val customSnoozeMinutes: String = "",
)

data class Assignments(
    val onCompletion: List<TaskAssignmentDetails>,
    val pastDue: List<TaskAssignmentDetails>,
    val dueToday: List<TaskAssignmentDetails>,
    val dueTomorrow: List<TaskAssignmentDetails>,
    val dueInFuture: List<TaskAssignmentDetails>,
    val otherAssignmentsCount: Map<String, Int>
) {
    fun isEmpty(): Boolean {
        return onCompletion.isEmpty() &&
                pastDue.isEmpty() &&
                dueToday.isEmpty() &&
                dueTomorrow.isEmpty() &&
                dueInFuture.isEmpty()

    }

    fun filterByListType(isShared: Boolean): Assignments {
        return Assignments(
            onCompletion = onCompletion.filter { it.isSharedList == isShared },
            pastDue = pastDue.filter { it.isSharedList == isShared },
            dueToday = dueToday.filter { it.isSharedList == isShared },
            dueTomorrow = dueTomorrow.filter { it.isSharedList == isShared },
            dueInFuture = dueInFuture.filter { it.isSharedList == isShared },
            otherAssignmentsCount = otherAssignmentsCount
        )
    }

    companion object {
        fun default(): Assignments {
            return Assignments(listOf(), listOf(), listOf(), listOf(), listOf(), mapOf())
        }
    }
}
