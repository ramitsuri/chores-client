package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.view.CompletedAssignmentsViewState
import com.ramitsuri.choresclient.model.view.ListTypeSelection
import com.ramitsuri.choresclient.model.view.filterByListTypes
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CompletedAssignmentsViewModel(
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager
) : ViewModel() {

    private val selectedListTypes = MutableStateFlow(ListTypeSelection.entries.toSet())

    val state = getCompleted().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CompletedAssignmentsViewState()
    )

    fun onListTypeClicked(listTypeSelection: ListTypeSelection) {
        selectedListTypes.update { currentSelection ->
            if (listTypeSelection in currentSelection) {
                if (currentSelection.size == 1) {
                    // Final selected type is clicked, unselect it + select everything else
                    ListTypeSelection.entries.toSet() - listTypeSelection
                } else {
                    currentSelection - listTypeSelection
                }
            } else {
                currentSelection + listTypeSelection
            }
        }
    }

    private fun getCompleted(): Flow<CompletedAssignmentsViewState> {
        val id = prefManager.getLoggedInMemberId()
        return if (id == null) {
            flowOf(CompletedAssignmentsViewState(loading = false))
        } else {
            combine(
                repository.getCompletedLocalFlow(id),
                selectedListTypes,
            ) { taskAssignmentDetails, selectedListTypes ->
                CompletedAssignmentsViewState(
                    loading = false,
                    assignments = taskAssignmentDetails
                        .filterByListTypes(selectedListTypes)
                        .sortedByDescending { it.taskAssignment.dueDateTime },
                    selectedListTypes = selectedListTypes,
                )
            }
        }
    }
}
