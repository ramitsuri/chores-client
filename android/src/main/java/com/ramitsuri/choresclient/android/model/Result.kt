package com.ramitsuri.choresclient.android.model

import com.ramitsuri.choresclient.android.ui.assigments.FilterMode

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: ViewError) : Result<Nothing>()
}

sealed class ViewState<out T> {
    data class Event(val event: ViewEvent): ViewState<Nothing>()
    data class Success<T>(val data: T) : ViewState<T>()
    data class Error(val error: ViewError) : ViewState<Nothing>()
}

data class AssignmentsViewState(
    val assignments: List<TaskAssignmentWrapper>,
    val selectedFilter: FilterMode
)

data class AssignmentDetailsViewState(
    val assignment: TaskAssignment
)

data class LoginViewState(
    val loggedIn: Boolean
)