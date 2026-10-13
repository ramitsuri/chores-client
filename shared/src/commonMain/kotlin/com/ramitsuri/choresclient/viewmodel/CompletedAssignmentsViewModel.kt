package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.view.CompletedAssignmentsViewState
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CompletedAssignmentsViewModel(
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager
) : ViewModel() {

    val state = getCompleted().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CompletedAssignmentsViewState()
    )

    private fun getCompleted(): Flow<CompletedAssignmentsViewState> {
        val id = prefManager.getLoggedInMemberId()
        return if (id == null) {
            flowOf(CompletedAssignmentsViewState(loading = false))
        } else {
            repository.getCompletedLocalFlow(id).map { taskAssignmentDetails ->
                CompletedAssignmentsViewState(
                    loading = false,
                    assignments = taskAssignmentDetails.sortedByDescending { it.taskAssignment.dueDateTime },
                )
            }
        }
    }
}
