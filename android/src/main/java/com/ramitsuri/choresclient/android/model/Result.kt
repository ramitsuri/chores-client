package com.ramitsuri.choresclient.android.model

import com.ramitsuri.choresclient.android.ui.assigments.FilterMode

sealed class Result<out T> {
    data class Success<T>(val data: T): Result<T>()
    data class Failure(val error: ViewError): Result<Nothing>()
}

sealed class ViewState<out T> {
    object Loading: ViewState<Nothing>()
    object Reload: ViewState<Nothing>()
    data class Success<T>(val data: T): ViewState<T>()
    data class Error(val error: ViewError): ViewState<Nothing>()
}

data class AssignmentsViewState(
    val assignments: List<TaskAssignmentWrapper>,
    val selectedFilter: FilterMode
)