package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem
import com.ramitsuri.choresclient.model.filter.FilterType
import com.ramitsuri.choresclient.model.view.CompletedAssignmentsViewState
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.FilterHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompletedAssignmentsViewModel(
    private val repository: TaskAssignmentsRepository,
    private val filterHelper: FilterHelper,
    private val prefManager: PrefManager
) : ViewModel() {

    private var collectJob: Job? = null

    private val _state = MutableStateFlow(CompletedAssignmentsViewState())
    val state: StateFlow<CompletedAssignmentsViewState> = _state

    private var rawAssignments: List<TaskAssignmentDetails> = emptyList()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(filters = filterHelper.getBaseFilters())
            }
            onFilterUpdated()
        }
    }

    fun onFilterItemClicked(filter: Filter, clickedFilterItem: FilterItem) {
        val existingFilters = _state.value.filters
        val newClickedFilter = filterHelper.onFilterItemClicked(filter, clickedFilterItem)
        val newFilters = existingFilters
            .filter { it.getType() != filter.getType() }
            .plus(newClickedFilter)
            .sortedBy { it.getType().index }
        _state.update {
            it.copy(filters = newFilters)
        }
        onFilterUpdated()
    }

    private fun onFilterUpdated() {
        collectJob?.cancel()

        val loggedInMemberId = prefManager.getLoggedInMemberId() ?: ""

        collectJob = viewModelScope.launch {
            repository.getCompletedLocalFlow(loggedInMemberId).collect { taskAssignmentDetails ->
                rawAssignments = taskAssignmentDetails.sortedByDescending { it.taskAssignment.dueDateTime }
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val filters = _state.value.filters
        val filtered = rawAssignments.toMutableList()

        filters.forEach { filter ->
            when (filter.getType()) {
                FilterType.PERSON -> {
                    if (!filter.getItems().any { it.getIsSelected() && it.getId() == Filter.ALL_ID }) {
                        val selectedMemberIds = filter.getItems().filter { it.getIsSelected() }.map { it.getId() }
                        filtered.removeAll { !selectedMemberIds.contains(it.taskAssignment.memberId) }
                    }
                }

                FilterType.HOUSE -> {
                    if (!filter.getItems().any { it.getIsSelected() && it.getId() == Filter.ALL_ID }) {
                        val selectedHouseIds = filter.getItems().filter { it.getIsSelected() }.map { it.getId() }
                        filtered.removeAll { !selectedHouseIds.contains(it.taskAssignment.houseId) }
                    }
                }
            }
        }

        _state.update {
            it.copy(
                loading = false,
                assignments = filtered
            )
        }
    }
}
