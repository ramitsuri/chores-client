package com.ramitsuri.choresclient.android.model

import com.ramitsuri.choresclient.android.ui.assigments.AssignmentDetails
import com.ramitsuri.choresclient.data.FilterMode
import com.ramitsuri.choresclient.data.ViewError

sealed class ViewState<out T> {
    data class Event(val event: ViewEvent) : ViewState<Nothing>()
    data class Success<T>(val data: T) : ViewState<T>()
    data class Error(val error: ViewError) : ViewState<Nothing>()
}

data class AssignmentsViewState(
    val assignments: List<TaskAssignmentWrapper>,
    val selectedFilter: FilterMode
)

data class AssignmentDetailsViewState(
    val assignment: AssignmentDetails
)

data class LoginViewState(
    val loggedIn: Boolean
)

enum class ViewEvent {
    LOADING,
    RELOAD,
    LOGIN
}