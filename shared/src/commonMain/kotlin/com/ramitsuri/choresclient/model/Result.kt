package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.FilterMode
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.ViewError
import kotlinx.datetime.Instant

sealed class ViewState<out T> {
    data class Event(val event: ViewEvent) : ViewState<Nothing>()
    data class Success<T>(val data: T) : ViewState<T>()
    data class Error(val error: ViewError) : ViewState<Nothing>()

    companion object {
        fun event(event: ViewEvent) = Event(event)
        fun <T> success(data: T) = Success(data)
        fun error(error: ViewError) = Error(error)
    }
}

data class AssignmentsViewState(
    val assignments: List<TaskAssignmentWrapper>,
    val selectedFilter: FilterMode
)

data class LoginViewState(
    val loggedIn: Boolean
)

data class AssignmentDetailsViewState(
    val assignment: AssignmentDetails
)

data class AssignmentDetails(
    val id: String,
    val name: String,
    val description: String,
    val repeatValue: Int,
    val repeatUnit: RepeatUnit,
    val notificationTime: Instant?
)

enum class ViewEvent {
    LOADING,
    RELOAD,
    LOGIN
}